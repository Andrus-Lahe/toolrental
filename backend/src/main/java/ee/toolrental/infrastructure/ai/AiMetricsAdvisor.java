package ee.toolrental.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

/**
 * Logib iga Gemini kutse tokenid, kestuse, mudeli ja finishReason'i.
 * Kasutaja küsimust ega AI vastust ei logita.
 */
@Slf4j
public class AiMetricsAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();

        ChatClientResponse response = chain.nextCall(request);

        long latencyMs = System.currentTimeMillis() - start;
        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse == null) {
            log.info("[AiMetrics] llm_call latencyMs={}", latencyMs);
            return response;
        }

        Usage usage = chatResponse.getMetadata().getUsage();
        Generation result = chatResponse.getResult();
        log.info("[AiMetrics] llm_call in={} out={} latencyMs={} model={} finishReason={}",
                usage == null ? null : usage.getPromptTokens(),
                usage == null ? null : usage.getCompletionTokens(),
                latencyMs,
                chatResponse.getMetadata().getModel(),
                result == null ? null : result.getMetadata().getFinishReason());

        return response;
    }

    @Override
    public String getName() {
        return "ai-metrics";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
