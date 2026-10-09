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

            Päring peab alati sisaldama tingimust is_available_now = TRUE.
            Tagasta ainult veerud tool_id, tool_name, category_name ja location_name.
            Rakenda kasutaja küsimusest arusaadavaid tööriista nime, kategooria või
            linnaosa filtreid ainult nende vaateveergude põhjal. Kui küsimuses filtrit
            pole, tagasta kõik vastavad tulemused. Järjesta tool_id järgi ja piira
            tulemused maksimaalselt 20 reani.

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
