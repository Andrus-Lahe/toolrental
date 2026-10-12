package ee.toolrental.aimodule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.google.genai.metadata.GoogleGenAiUsage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class AiTokenUsageLogger implements CallAdvisor, ChatClientBuilderCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(AiTokenUsageLogger.class);
    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    // Each synchronous ToolAskService invocation owns its counters, even under concurrent requests.
    private final ThreadLocal<RequestUsage> currentRequest = new ThreadLocal<>();
    private final BigDecimal inputUsdPerMillion;
    private final BigDecimal outputUsdPerMillion;
    private final BigDecimal cachedInputUsdPerMillion;
    private final BigDecimal usdPerEur;

    public AiTokenUsageLogger(
            @Value("${toolrental.ai.token-pricing.input-usd-per-million}") BigDecimal inputUsdPerMillion,
            @Value("${toolrental.ai.token-pricing.output-usd-per-million}") BigDecimal outputUsdPerMillion,
            @Value("${toolrental.ai.token-pricing.cached-input-usd-per-million}") BigDecimal cachedInputUsdPerMillion,
            @Value("${toolrental.ai.token-pricing.usd-per-eur}") BigDecimal usdPerEur) {
        if (inputUsdPerMillion.signum() < 0 || outputUsdPerMillion.signum() < 0
                || cachedInputUsdPerMillion.signum() < 0 || usdPerEur.signum() <= 0) {
            throw new IllegalArgumentException("Token prices must be non-negative and USD per EUR must be positive");
        }
        this.inputUsdPerMillion = inputUsdPerMillion;
        this.outputUsdPerMillion = outputUsdPerMillion;
        this.cachedInputUsdPerMillion = cachedInputUsdPerMillion;
        this.usdPerEur = usdPerEur;
    }

    public <T> T logRequest(Supplier<T> action) {
        RequestUsage request = new RequestUsage();
        currentRequest.set(request);
        try {
            return action.get();
        } finally {
            currentRequest.remove();
            log(request);
        }
    }

    public void secondRequest() {
        currentRequest.get().secondRequest = true;
    }

    @Override
    public void customize(ChatClient.Builder builder) {
        builder.defaultAdvisors(this);
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        RequestUsage current = currentRequest.get();
        if (current == null) return chain.nextCall(request);
        CallUsage call = current.secondRequest ? current.second : current.first;
        long startedAt = System.nanoTime();
        try {
            ChatClientResponse response = chain.nextCall(request);
            Usage usage = response.chatResponse() == null ? null : response.chatResponse().getMetadata().getUsage();
            if (usage == null || usage.getPromptTokens() == null || usage.getCompletionTokens() == null
                    || usage instanceof GoogleGenAiUsage && usage.getNativeUsage() == null) {
                call.unknown = true;
            } else {
                call.input += usage.getPromptTokens();
                call.output += usage.getCompletionTokens();
                if (usage instanceof GoogleGenAiUsage googleUsage && googleUsage.getThoughtsTokenCount() != null) {
                    call.output += googleUsage.getThoughtsTokenCount();
                }
                Long cachedTokens = usage.getCacheReadInputTokens();
                call.cachedInput += cachedTokens == null ? 0 : cachedTokens;
            }
            return response;
        } catch (RuntimeException exception) {
            call.unknown = true;
            throw exception;
        } finally {
            call.latencyMillis += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        }
    }

    @Override
    public String getName() {
        return "AiTokenUsageLogger";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private void log(RequestUsage request) {
        CallUsage first = request.first;
        CallUsage second = request.second;
        boolean unknown = first.unknown || second.unknown;
        BigDecimal firstOutput = cost(first.output, outputUsdPerMillion);
        BigDecimal firstInput = inputCost(first);
        BigDecimal secondOutput = cost(second.output, outputUsdPerMillion);
        BigDecimal secondInput = inputCost(second);
        // One event keeps the five lines together when multiple questions finish simultaneously.
        LOG.info("\nFirst request Output token: [amount: {}] [price: {}] [latency: {}ms]"
                        + "\nFirst request Input token: [amount: {}] [price: {}] [latency: {}ms]"
                        + "\nSecond request Output token: [amount: {}] [price: {}] [latency: {}ms]"
                        + "\nSecond request Input token: [amount: {}] [price: {}] [latency: {}ms]"
                        + "\nTotal: {} {}",
                amount(first.output, first.unknown), price(firstOutput, first.unknown), first.latencyMillis,
                amount(first.input, first.unknown), price(firstInput, first.unknown), first.latencyMillis,
                amount(second.output, second.unknown), price(secondOutput, second.unknown), second.latencyMillis,
                amount(second.input, second.unknown), price(secondInput, second.unknown), second.latencyMillis,
                amount(first.input + first.output + second.input + second.output, unknown),
                price(firstOutput.add(firstInput).add(secondOutput).add(secondInput), unknown));
    }

    private BigDecimal inputCost(CallUsage call) {
        return cost(call.input - call.cachedInput, inputUsdPerMillion)
                .add(cost(call.cachedInput, cachedInputUsdPerMillion));
    }

    private BigDecimal cost(long tokens, BigDecimal usdPerMillion) {
        return BigDecimal.valueOf(tokens).multiply(usdPerMillion)
                .divide(MILLION.multiply(usdPerEur), 8, RoundingMode.HALF_UP);
    }

    private static String amount(long tokens, boolean unknown) {
        return unknown ? "N/A" : Long.toString(tokens);
    }

    private static String price(BigDecimal eur, boolean unknown) {
        return unknown ? "N/A" : eur.setScale(8, RoundingMode.HALF_UP).toPlainString() + " €";
    }

    private static class RequestUsage {
        private final CallUsage first = new CallUsage();
        private final CallUsage second = new CallUsage();
        private boolean secondRequest;
    }

    private static class CallUsage {
        private long input;
        private long output;
        private long cachedInput;
        private long latencyMillis;
        private boolean unknown;
    }
}
