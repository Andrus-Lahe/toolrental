package ee.toolrental.service;

import ee.toolrental.controller.ask.dto.AskResponse;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class NlToSqlService {

    private static final String SQL_SYSTEM_PROMPT = """
            You are a PostgreSQL query generator for a tool rental database.
            All tables are in schema tool_rental: always write table names as tool_rental.<table>.

            SCHEMA:
            role(id INTEGER PK, role_name TEXT)  -- 'admin' or 'customer'
            app_user(id INTEGER PK, role_id INTEGER FK role.id, first_name TEXT, last_name TEXT, status CHAR)  -- status: 'A' active, 'B' blocked
            city(id INTEGER PK, city_name TEXT)
            district(id INTEGER PK, city_id INTEGER FK city.id, district_name TEXT)
            location(id INTEGER PK, district_id INTEGER FK district.id, street_name TEXT, house_number TEXT, apartment_number TEXT)
            profile(id INTEGER PK, user_id INTEGER FK app_user.id, location_id INTEGER FK location.id, email TEXT, phone TEXT, created_at TIMESTAMP, updated_at TIMESTAMP)
            category(id INTEGER PK, category_name TEXT, description TEXT, sequence INTEGER)
            tool(id INTEGER PK, owner_id INTEGER FK app_user.id, category_id INTEGER FK category.id, name TEXT, description TEXT, status CHAR, created_at TIMESTAMP, updated_at TIMESTAMP)  -- status: 'A' available, 'U' unavailable
            booking(id INTEGER PK, tool_id INTEGER FK tool.id, renter_id INTEGER FK app_user.id, start_date DATE, end_date DATE, status CHAR, owner_message TEXT, created_at TIMESTAMP, updated_at TIMESTAMP)  -- status: 'P' pending, 'C' confirmed, 'R' rejected

            RULES:
            1. Return only one valid SQL query ending with a semicolon: no explanations, no comments, no markdown code fences.
            2. Use only the tables and columns listed in SCHEMA. If the question cannot be answered from the schema, return exactly: -- CANNOT_ANSWER
            3. Generate only read-only SELECT queries. Never produce INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE or any other data-changing statement.
            If asked to change data, return exactly: -- REFUSED
            4. Use only PostgreSQL SQL syntax and functions.
            5. The user may ask in Estonian.
            """;

    private static final String SQL_USER_PROMPT_TEMPLATE = """
            Generate SQL syntax for the question below

            %s
            """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are a helpful assistant that summarizes data clearly. Always answer in Estonian.""";

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            Summarize this database query result in 1-3 plain sentences.
            Don't mention SQL or technical terms. Be specific about numbers.

            Question: %s
            Results (%d rows): %s
            """;

    private static final String CANNOT_ANSWER_MARKER = "CANNOT_ANSWER";
    private static final String REFUSED_MARKER = "REFUSED";

    private static final String CANNOT_ANSWER_MESSAGE = "Vabandust, sellele küsimusele ei saa olemasolevate andmete põhjal vastata.";
    private static final String REFUSED_MESSAGE = "Vabandust, ma saan andmeid ainult lugeda. Muuta, lisada ega kustutada ei saa.";
    private static final String SQL_NOT_ALLOWED_MESSAGE = "Lubatud on ainult andmete lugemine (SELECT päringud)";
    private static final String SQL_NOT_ALLOWED = "SQL_NOT_ALLOWED";
    private static final String EMPTY_AI_RESPONSE_MESSAGE = "AI mudel ei andnud vastust. Palun proovi uuesti.";
    private static final String QUERY_FAILED_MESSAGE = "Päringut ei õnnestunud käivitada. Palun sõnasta küsimus ümber.";

    private static final int MAX_RESULT_ROWS = 100;
    private static final int QUERY_TIMEOUT_SECONDS = 10;

    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate readOnlyTransactionTemplate;

    // Konstruktor: Spring annab kaasa AI kliendi ehitaja, andmebaasi ühenduse (DataSource) ja tehingute halduri.
    // Loob oma JdbcTemplate'i, mis tagastab kuni 100 rida ja katkestab päringu 10 sekundi järel,
    // ning TransactionTemplate'i, mis käivitab päringu ainult lugemiseks mõeldud tehingus.
    public NlToSqlService(ChatClient.Builder chatClientBuilder, DataSource dataSource,
                          PlatformTransactionManager transactionManager) {
        this.chatClient = chatClientBuilder.build();

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcTemplate.setMaxRows(MAX_RESULT_ROWS);
        this.jdbcTemplate.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

        this.readOnlyTransactionTemplate = new TransactionTemplate(transactionManager);
        this.readOnlyTransactionTemplate.setReadOnly(true);
    }

    // Põhimeetod: võtab kasutaja küsimuse tavakeeles ja laseb AI-l sellest SQL päringu koostada.
    // Kui AI tagastab CANNOT_ANSWER või REFUSED markeri, vastatakse viisakalt ilma päringut tegemata;
    // muidu SQL kontrollitakse, käivitatakse andmebaasis ja AI võtab tulemuse inimkeeles kokku.
    public AskResponse ask(String userQuestion) {
        String generatedSql = callLlm(SQL_SYSTEM_PROMPT, SQL_USER_PROMPT_TEMPLATE.formatted(userQuestion));

        if (isMarker(generatedSql, CANNOT_ANSWER_MARKER)) {
            return new AskResponse(CANNOT_ANSWER_MESSAGE);
        }
        if (isMarker(generatedSql, REFUSED_MARKER)) {
            return new AskResponse(REFUSED_MESSAGE);
        }

        validateSqlQuery(generatedSql);
        List<Map<String, Object>> databaseResults = runReadOnlyQuery(generatedSql);
        return generateResponse(userQuestion, databaseResults);
    }

    // Kontrollib, kas AI vastus on eriline marker (nt "-- CANNOT_ANSWER") päris SQL-i asemel.
    // Eemaldab alguse "--" kommentaarimärgi ja võrdleb tõstutundetult, lubades lõpus ka semikoolonit.
    private boolean isMarker(String generatedSql, String marker) {
        String withoutCommentPrefix = generatedSql.replaceFirst("^--\\s*", "");
        return withoutCommentPrefix.equalsIgnoreCase(marker)
                || withoutCommentPrefix.equalsIgnoreCase(marker + ";");
    }

    // Turvakontroll enne päringu käivitamist: lubatud on ainult SELECT päringud.
    // Kui SQL sisaldab andmeid muutvaid võtmesõnu (DROP, DELETE, UPDATE jne), visatakse ForbiddenException (403).
    private void validateSqlQuery(String generatedSql) {
        String upperCaseSql = generatedSql.toUpperCase();
        if (!upperCaseSql.startsWith("SELECT")
                || upperCaseSql.matches("(?s).*\\b(DROP|DELETE|INSERT|UPDATE|TRUNCATE|ALTER|GRANT|COPY|CALL|DO)\\b.*")) {
            throw new ForbiddenException(SQL_NOT_ALLOWED_MESSAGE, SQL_NOT_ALLOWED);
        }
    }

    // Käivitab AI koostatud SQL päringu toolrentali andmebaasis ainult lugemiseks mõeldud tehingus,
    // nii et PostgreSQL keelduks andmete muutmisest ka siis, kui eelmine kontroll midagi märkamata jättis.
    // Andmebaasi viga (nt AI mõtles välja olematu veeru) muudetakse 500 vastuseks.
    private List<Map<String, Object>> runReadOnlyQuery(String generatedSql) {
        try {
            return readOnlyTransactionTemplate.execute(status -> jdbcTemplate.queryForList(generatedSql));
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(QUERY_FAILED_MESSAGE);
        }
    }

    // Koostab kasutajale lõpliku vastuse andmebaasi tulemuste põhjal.
    // Laseb AI-l tulemused kokku võtta ja pakendab selle teksti uude AskResponse objekti.
    private AskResponse generateResponse(String userQuestion, List<Map<String, Object>> databaseResults) {
        String answer = formatResult(userQuestion, databaseResults);
        return new AskResponse(answer);
    }

    // Ehitab kokkuvõtte prompti: algne küsimus, ridade arv ja päringu tulemused.
    // Saadab selle AI-le, kes kirjutab tulemusest 1-3 lauset lihtsas eesti keeles.
    private String formatResult(String userQuestion, List<Map<String, Object>> databaseResults) {
        String userPrompt = SUMMARY_USER_PROMPT_TEMPLATE.formatted(
                userQuestion,
                databaseResults.size(),
                databaseResults);
        return callLlm(SUMMARY_SYSTEM_PROMPT, userPrompt);
    }

    // Üldine abimeetod AI mudeli (LLM) poole pöördumiseks süsteemi- ja kasutajapromptiga.
    // Vastus loetakse tavalise tekstina (.content()), sest mudel ei vorminda seda alati JSON-iks;
    // tühja vastuse korral visatakse InternalServerErrorException (500), muidu eemaldatakse ümbritsevad tühikud.
    private String callLlm(String systemPrompt, String userPrompt) {
        String content = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new InternalServerErrorException(EMPTY_AI_RESPONSE_MESSAGE);
        }
        return content.strip();
    }
}
