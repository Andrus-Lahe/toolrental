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
        log.debug("Tool search intent reasoning: {}", intent.reasoning());

        String city = canonicalName(intent.city(), cities, question);
        String district = canonicalName(intent.district(), districts, question);
        String category = canonicalName(intent.category(), categories, question);
        if (unknownName(intent.city(), city) || unknownName(intent.district(), district)
                || unknownName(intent.category(), category)) {
            return new ToolAskResponse("Sellele otsingule ma tulemusi ei leidnud, küsi midagi muud", List.of());
        }

        List<AvailableTool> tools = repository.findAvailable(city, district, category);
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
