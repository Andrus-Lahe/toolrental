package ee.toolrental.aimodule;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ToolSearchSqlGenerator {

    public static final String CANNOT_ANSWER = "CANNOT_ANSWER";

    private static final String SYSTEM_INSTRUCTIONS = """
            Koosta tööriistaotsingu küsimuse põhjal üks PostgreSQL SELECT päring.
            Kasuta ainult allpool kirjeldatud vaadet ja selle veerge:
            tool_rental.ai_tool_search(
              tool_id integer,
              tool_name varchar,
              tool_description varchar või NULL,
              category_name varchar,
              location_name varchar või NULL,
              is_available_now boolean
            ).

            Kasuta täpselt seda lihtsat päringukuju:
            SELECT tool_id, tool_name, category_name, location_name
            FROM tool_rental.ai_tool_search
            WHERE is_available_now = TRUE
            ORDER BY tool_id ASC LIMIT 20
            Ära muuda projektsiooniveergude järjekorda ega lisa aliaseid.
            Tuvasta küsimusest vajalik töö tüüp ja otsi andmebaasist selleks sobivaid
            tööriistu nime või kirjelduse järgi. Ära nõua, et küsimuse sõnad esineksid
            tööriista andmetes; jäta ebaolulised detailid filtritest välja. Vali sobivad
            lubatud filtrid ise; ära kasuta Java-poolseid sünonüümireegleid. Kui küsimus
            kirjeldab mitut vajadust, otsi igaühe jaoks sobivaid tööriistu ja ühenda
            alternatiivsed tekstivasted OR-iga, et üks vajadus ei välistaks teist.
            Lisa saadavustingimuse järel üks või mitu filtrit AND-iga. Mitme alternatiivse
            tekstitabamuse jaoks kasuta sulgudes OR-rühma, näiteks
            (tool_name ILIKE '%labidas%' OR tool_description ILIKE '%labidas%'), ning lisa
            asukoha/kategooria piirangud eraldi AND-tingimustena.
            Tekstifiltrites kasuta ainult =, LIKE või ILIKE operaatorit ja ülakomadega
            stringi; ID-filtris ainult täisarvu võrdlust. Lubatud otsinguväljad on tööriista
            nimi, kirjeldus, kategooria, asukoht ja ID. Kirjeldus on ainult WHERE filtri jaoks:
            ära lisa seda SELECT projektsiooni.
            Ära kasuta alampäringuid, JOIN-e, funktsioone ega muid tabeleid.
            Kui küsimuses filtrit pole, kasuta ainult saadavuse tingimust.
            LIMIT peab olema täisarv vahemikus 1 kuni 20.

            Väljasta ainult päringutekst: üks SELECT, ilma Markdowni, koodiploki,
            kommentaaride või selgituseta. Ära koosta andmete muutmise päringuid.
            Kui küsimusele ei saa selle vaate põhjal vastata, väljasta täpselt
            CANNOT_ANSWER ja mitte midagi muud.
            """;

    private final ChatClient chatClient;

    public ToolSearchSqlGenerator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateSql(String question) {
        return generateSql(question, null);
    }

    public String repairSql(String question, InvalidToolSearchSqlException validationFailure) {
        return generateSql(question, validationFailure.safeFeedback());
    }

    private String generateSql(String question, String validationFeedback) {
        String userPrompt = question;
        if (validationFeedback != null) {
            userPrompt = "Algne küsimus: " + question + "\nValidaatori tagasiside: "
                    + validationFeedback + "\nTagasta üks parandatud päring.";
        }
        String sql = chatClient.prompt()
                .system(SYSTEM_INSTRUCTIONS)
                .user(userPrompt)
                .call()
                .content();

        if (sql == null || sql.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty SQL query");
        }
        return sql.trim();
    }
}
