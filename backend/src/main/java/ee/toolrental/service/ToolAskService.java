package ee.toolrental.service;

import ee.toolrental.controller.ai.ToolAskResponse;
import ee.toolrental.infrastructure.ai.AiMetricsAdvisor;
import ee.toolrental.infrastructure.ai.RegexJailbreakAdvisor;
import ee.toolrental.persistence.ai.AvailableTool;
import ee.toolrental.persistence.ai.AvailableToolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ToolAskService {

    // E-post, URL või telefoninumber AI vastuses: vastus asendatakse tööriistade nimedest koostatud tekstiga.
    private static final Pattern UNSAFE_OUTPUT_PATTERN = Pattern.compile(
            "[\\w.+-]+@[\\w-]+\\.[\\w.]+|(https?://|www\\.)|\\+?\\d[\\d\\s-]{6,}\\d",
            Pattern.CASE_INSENSITIVE);
    private static final int TOOL_NAME_MAX_LENGTH = 100;
    // Nii mitu esimest tähte tööriista nimest peab olema küsimuses, muidu on nimi mudeli enda välja mõeldud.
    private static final int TOOL_NAME_PREFIX_LENGTH = 5;

    private final AvailableToolRepository repository;
    private final ChatClient chatClient;
    private final RegexJailbreakAdvisor regexJailbreakAdvisor;
    private final String intentPrompt;
    private final String answerPrompt;
    private final GoogleGenAiThinkingLevel thinkingLevel;
    private final Integer intentMaxOutputTokens;
    private final Integer answerMaxOutputTokens;
    private final Integer answerMaxLength;

    public ToolAskService(AvailableToolRepository repository,
                          ChatClient.Builder chatClientBuilder,
                          @Value("classpath:prompts/tool-search-intent.md") Resource intentPromptResource,
                          @Value("classpath:prompts/tool-search-answer.md") Resource answerPromptResource,
                          @Value("${toolrental.ai.thinking-level}") GoogleGenAiThinkingLevel thinkingLevel,
                          @Value("${toolrental.ai.intent.max-output-tokens}") Integer intentMaxOutputTokens,
                          @Value("${toolrental.ai.answer.max-output-tokens}") Integer answerMaxOutputTokens,
                          @Value("${toolrental.ai.answer.max-length}") Integer answerMaxLength) throws IOException {
        this.repository = repository;
        this.chatClient = chatClientBuilder.defaultAdvisors(new AiMetricsAdvisor()).build();
        this.regexJailbreakAdvisor = new RegexJailbreakAdvisor();
        // Promptid loetakse käivitumisel, et puuduv fail annaks vea kohe, mitte esimese päringu ajal.
        this.intentPrompt = intentPromptResource.getContentAsString(StandardCharsets.UTF_8);
        this.answerPrompt = answerPromptResource.getContentAsString(StandardCharsets.UTF_8);
        this.thinkingLevel = thinkingLevel;
        this.intentMaxOutputTokens = intentMaxOutputTokens;
        this.answerMaxOutputTokens = answerMaxOutputTokens;
        this.answerMaxLength = answerMaxLength;
    }

    public ToolAskResponse ask(String question) {
        List<String> cities = repository.cityNames();
        List<String> districts = repository.districtNames();
        List<String> categories = repository.categoryNames();

        // .param() ei kasutata: siis Spring AI ei renderda teksti mallina ja few-shot JSON-i {} jääb puutumata.
        ToolSearchIntent intent = chatClient.prompt()
                .advisors(a -> a.advisors(regexJailbreakAdvisor).param(RegexJailbreakAdvisor.QUESTION_PARAM, question))
                .options(GoogleGenAiChatOptions.builder()
                        .thinkingLevel(thinkingLevel)
                        .maxOutputTokens(intentMaxOutputTokens))
                .system(intentPrompt)
                .user("""
                        Linnad: %s
                        Linnaosad: %s
                        Kategooriad: %s
                        <question>%s</question>
                        """.formatted(cities, districts, categories, question))
                .call()
                .entity(ToolSearchIntent.class, spec -> spec.useProviderStructuredOutput());

        if (intent == null || !intent.toolSearch()) {
            return new ToolAskResponse("Saan sind aidata küll, aga ainult saadaval olevate tööriistade otsimisega.", List.of());
        }
        // Jutumärkides on mudeli antud tekst, ilma jutumärkideta null tähendab, et välja polnud.
        log.debug("Tool search intent: reasoning={} city={} district={} category={} toolName={}",
                intent.reasoning(), quoted(intent.city()), quoted(intent.district()),
                quoted(intent.category()), quoted(intent.toolName()));

        String requestedCity = normalizeIntentValue(intent.city());
        String requestedDistrict = normalizeIntentValue(intent.district());
        String requestedCategory = normalizeIntentValue(intent.category());
        String requestedToolName = normalizeIntentValue(intent.toolName());
        String city = canonicalName(requestedCity, cities, question);
        String district = canonicalName(requestedDistrict, districts, question);
        String category = canonicalName(requestedCategory, categories, question);
        if (unknownName(requestedCity, city) || unknownName(requestedDistrict, district)
                || unknownName(requestedCategory, category)
                || (requestedToolName != null && requestedToolName.length() > TOOL_NAME_MAX_LENGTH)) {
            return new ToolAskResponse("Sellele otsingule ma tulemusi ei leidnud, küsi midagi muud", List.of());
        }
        String toolName = groundedToolName(requestedToolName, question);

        List<AvailableTool> tools = repository.findAvailable(city, district, category, toolName);
        if (tools.isEmpty()) {
            return new ToolAskResponse("Sellele otsingule ma vastavaid saadaval tööriistu ei leidnud.", List.of());
        }

        String toolData = tools.stream()
                .map(tool -> "%d | %s | %s | %s | %s".formatted(
                        tool.toolId(), tool.name(), tool.category(), tool.city(), tool.district()))
                .collect(Collectors.joining("\n"));
        String answer = chatClient.prompt()
                .options(GoogleGenAiChatOptions.builder()
                        .thinkingLevel(thinkingLevel)
                        .maxOutputTokens(answerMaxOutputTokens))
                .system(answerPrompt)
                .user("""
                        <question>%s</question>
                        <tools>
                        ID | nimi | kategooria | linn | linnaosa
                        %s
                        </tools>
                        """.formatted(question, toolData))
                .call()
                .content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("Gemini returned an empty answer");
        }
        return new ToolAskResponse(getSafeAnswer(answer, tools), tools);
    }

    // Skeemis on väljad tüübiga string, seega tagastab Gemini puuduva väärtuse kord "", kord tekstina "null".
    private String normalizeIntentValue(String value) {
        if (value == null || value.isBlank() || value.trim().equalsIgnoreCase("null")) {
            return null;
        }
        return value.trim();
    }

    private String quoted(String value) {
        return value == null ? null : "\"" + value + "\"";
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

    // Mudel võib tööriista nime ise välja mõelda (nt "koristada" → "Koristusvahendid" või "Kõrgsurvepesur").
    // Nimi jäetakse kõrvale, kui selle algust küsimuses pole või kui ühegi tööriista nimes seda pole.
    // Siis otsitakse ainult linna, linnaosa ja kategooria järgi.
    private String groundedToolName(String requested, String question) {
        if (requested == null || requested.isBlank()) {
            return null;
        }
        String lowerToolName = requested.toLowerCase(Locale.ROOT);
        String prefix = lowerToolName.substring(0, Math.min(TOOL_NAME_PREFIX_LENGTH, lowerToolName.length()));
        if (!question.toLowerCase(Locale.ROOT).contains(prefix)) {
            log.debug("Tool name not found in question, ignored: {}", requested);
            return null;
        }
        boolean toolExists = repository.toolNames().stream()
                .anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(lowerToolName));
        if (!toolExists) {
            log.debug("Tool name not found in database, ignored: {}", requested);
            return null;
        }
        return requested;
    }

    private boolean unknownName(String requested, String canonical) {
        return requested != null && !requested.isBlank() && canonical == null;
    }

    private String getSafeAnswer(String answer, List<AvailableTool> tools) {
        if (answer.length() <= answerMaxLength && !UNSAFE_OUTPUT_PATTERN.matcher(answer).find()) {
            return answer.trim();
        }
        log.warn("Unsafe AI answer replaced");
        return tools.stream()
                .map(AvailableTool::name)
                .collect(Collectors.joining(", ", "Leidsin sulle järgmised saadaval tööriistad: ", "."));
    }
}
