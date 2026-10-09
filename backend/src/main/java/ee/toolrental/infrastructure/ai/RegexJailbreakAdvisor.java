package ee.toolrental.infrastructure.ai;

import ee.toolrental.infrastructure.exception.GuardrailViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.Ordered;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Deterministlik regex-filter prompt injection ja jailbreak katsete vastu.
 * Kiire ja tasuta esimene kaitsekiht, kuid tabab ainult teadaolevaid mustreid.
 * Kontrollib advisori parameetrit QUESTION_PARAM (ainult kasutaja küsimus), mitte kogu user-sõnumit.
 */
@Slf4j
public class RegexJailbreakAdvisor implements CallAdvisor {

    public static final String QUESTION_PARAM = "toolrental.question";

    // Mustrid rakendatakse normaliseeritud (väiketähed, üks tühik) tekstile.
    private static final Map<String, Pattern> PATTERNS = Map.ofEntries(
            Map.entry("ignore-instructions", Pattern.compile(
                    "(ignore|disregard|forget|override|bypass)\\s+(all\\s+|any\\s+|the\\s+)?(previous|prior|above|earlier|your|system)\\s+(instructions?|prompts?|rules?|guidelines?)")),
            Map.entry("ignore-instructions-et", Pattern.compile(
                    "(ignoreeri|unusta|eira)\\s+(kõiki\\s+|kõik\\s+)?(eelnevaid|eelmisi|varasemaid|oma)?\\s*(juhiseid|juhised|reegleid|reeglid)",
                    Pattern.UNICODE_CHARACTER_CLASS)),
            Map.entry("role-override", Pattern.compile(
                    "\\b(you are now|from now on,? you|act as (a|an|my)|pretend (to be|you are)|roleplay as)\\b")),
            Map.entry("role-override-et", Pattern.compile(
                    "\\b(sa oled nüüd|teeskle,? et|käitu nagu|mängi,? et oled)\\b",
                    Pattern.UNICODE_CHARACTER_CLASS)),
            Map.entry("known-jailbreak", Pattern.compile(
                    "\\b(dan mode|do anything now|developer mode|jailbreak|god mode|unfiltered mode)\\b")),
            Map.entry("prompt-extraction", Pattern.compile(
                    "(reveal|show|print|repeat|output|tell me)\\s+(me\\s+)?(your|the)\\s+(system\\s+)?(prompt|instructions|rules)")),
            Map.entry("prompt-extraction-et", Pattern.compile(
                    "(näita|ütle|korda|kirjuta välja)\\s+(mulle\\s+)?(oma\\s+)?(süsteemi\\s*)?(prompti?|juhiseid|juhised|reegleid|reeglid)",
                    Pattern.UNICODE_CHARACTER_CLASS)),
            Map.entry("fake-role-markers", Pattern.compile(
                    "(<\\|?(im_start|im_end|system|endoftext)\\|?>|\\[/?inst]|^\\s*(system|assistant)\\s*:|###\\s*(system|instruction))",
                    Pattern.MULTILINE)),
            Map.entry("encoded-payload", Pattern.compile(
                    "[a-z0-9+/]{120,}={0,2}")) // pikk base64-laadne plokk
    );

    private static final Pattern INVISIBLE_CHARS = Pattern.compile("[\\u200B-\\u200F\\u202A-\\u202E\\u2060-\\u2064\\uFEFF]");

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String normalized = normalize(getQuestion(request));
        PATTERNS.forEach((name, pattern) -> {
            if (pattern.matcher(normalized).find()) {
                throw block(name, "Matched jailbreak pattern '" + name + "'");
            }
        });
        return chain.nextCall(request);
    }

    private String getQuestion(ChatClientRequest request) {
        if (request.context().get(QUESTION_PARAM) instanceof String question) {
            return question;
        }
        UserMessage userMessage = request.prompt().getUserMessage();
        return userMessage == null || userMessage.getText() == null ? "" : userMessage.getText();
    }

    /**
     * Ründajad proovivad filtrist mööda hiilida nt nähtamatute märkide, täislaiuses tähtede
     * ("ｉｇｎｏｒｅ") või lisatühikutega - normaliseerime need enne kontrolli ära.
     */
    static String normalize(String text) {
        String nfkc = Normalizer.normalize(text, Normalizer.Form.NFKC);
        return INVISIBLE_CHARS.matcher(nfkc).replaceAll("")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\h+", " "); // reavahetused jäävad alles, et ^system: muster töötaks
    }

    private GuardrailViolationException block(String rule, String reason) {
        log.warn("[RegexJailbreak] blocked rule={}", rule);
        return new GuardrailViolationException(getName() + ":" + rule, reason);
    }

    @Override
    public String getName() {
        return "regex-jailbreak-guard";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
