package ee.toolrental.service;

import ee.toolrental.controller.ai.ToolAskResponse;
import ee.toolrental.persistence.ai.AvailableTool;
import ee.toolrental.persistence.ai.AvailableToolRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ToolAskService {

    private static final String SEARCH_INSTRUCTIONS = """
            Tuvasta, kas küsimus küsib laenutatavate tööriistade kohta.
            Tagasta toolSearch=false, kui küsitakse kasutajaid, kontakte, broneeringuid või muid andmeid.
            Kui küsimus on tööriistade kohta, tagasta toolSearch=true ja vajadusel city,
            district ning category. Kasuta ainult antud loendites olevaid täpseid nimesid.
            Eesti käänded (nt Kristiines) tuleb teisendada loendi algvormiks (Kristiine).
            Kui filtrit ei küsitud, jäta selle väärtus nulliks. Ära genereeri SQL-i.
            """;

    private final AvailableToolRepository repository;
    private final ChatClient chatClient;

    public ToolAskService(AvailableToolRepository repository, ChatClient.Builder chatClientBuilder) {
        this.repository = repository;
        this.chatClient = chatClientBuilder.build();
    }

    public ToolAskResponse ask(String question) {
        List<String> cities = repository.cityNames();
        List<String> districts = repository.districtNames();
        List<String> categories = repository.categoryNames();

        ToolSearchIntent intent = chatClient.prompt()
                .system(SEARCH_INSTRUCTIONS)
                .user("""
                        Linnad: %s
                        Linnaosad: %s
                        Kategooriad: %s
                        Küsimus: %s
                        """.formatted(cities, districts, categories, question))
                .call()
                .entity(ToolSearchIntent.class);

        if (intent == null || !intent.toolSearch()) {
            return new ToolAskResponse("Saan aidata ainult saadaval tööriistade otsimisega.", List.of());
        }

        String city = canonicalName(intent.city(), cities, question);
        String district = canonicalName(intent.district(), districts, question);
        String category = canonicalName(intent.category(), categories, question);
        if (unknownName(intent.city(), city) || unknownName(intent.district(), district)
                || unknownName(intent.category(), category)) {
            return new ToolAskResponse("Sellist linna, linnaosa või kategooriat ei leitud.", List.of());
        }

        List<AvailableTool> tools = repository.findAvailable(city, district, category);
        if (tools.isEmpty()) {
            return new ToolAskResponse("Sellele otsingule vastavaid saadaval tööriistu ei leitud.", List.of());
        }

        String toolData = tools.stream()
                .map(tool -> "%d | %s | %s | %s | %s".formatted(
                        tool.toolId(), tool.name(), tool.category(), tool.city(), tool.district()))
                .collect(Collectors.joining("\n"));
        String answer = chatClient.prompt()
                .system("""
                        Vasta eesti keeles kasutaja tööriistaotsingu küsimusele. Kasuta ainult antud
                        tööriistade nimesid, kategooriaid ja asukohti. Ära lisa oletusi hindade,
                        omanike, kontaktide, broneeringute ega piltide kohta. Loendis võib olla
                        ainult esimesed 20 tulemust; ära väida, et see on täielik loend.
                        """)
                .user("Küsimus: %s\nLeitud tööriistad (ID | nimi | kategooria | linn | linnaosa):\n%s"
                        .formatted(question, toolData))
                .call()
                .content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty answer");
        }
        return new ToolAskResponse(answer, tools);
    }

    private String canonicalName(String requested, List<String> names, String question) {
        if (requested != null && !requested.isBlank()) {
            String exactName = names.stream()
                    .filter(name -> name.equalsIgnoreCase(requested.trim()))
                    .findFirst()
                    .orElse(null);
            if (exactName != null) {
                return exactName;
            }
        }
        String lowerQuestion = question.toLowerCase(Locale.ROOT);
        return names.stream()
                .filter(name -> lowerQuestion.contains(name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
    }

    private boolean unknownName(String requested, String canonical) {
        return requested != null && !requested.isBlank() && canonical == null;
    }
}
