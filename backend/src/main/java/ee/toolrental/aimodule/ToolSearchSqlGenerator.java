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
            Kui kasutaja küsis filtreid, lisa need saadavustingimuse järel ja enne
            järjestamist eraldi AND-tingimustena.
            Rakenda kasutaja küsimusest arusaadavaid tööriista nime, kategooria,
            asukoha või ID filtreid ainult nende vaateveergude põhjal. Tekstifiltrites
            kasuta ainult =, LIKE või ILIKE operaatorit ja ülakomadega stringi;
            ID-filtris kasuta ainult täisarvu võrdlust. Seo filtrid ainult AND-iga.
            Ära kasuta OR-i, sulge, alampäringuid, JOIN-e, funktsioone ega muid tabeleid.
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
        String sql = chatClient.prompt()
                .system(SYSTEM_INSTRUCTIONS)
                .user(question)
                .call()
                .content();

        if (sql == null || sql.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty SQL query");
        }
        return sql.trim();
    }
}
