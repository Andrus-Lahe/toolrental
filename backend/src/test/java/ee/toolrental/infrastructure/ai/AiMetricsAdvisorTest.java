package ee.toolrental.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiMetricsAdvisorTest {

    private final AiMetricsAdvisor aiMetricsAdvisor = new AiMetricsAdvisor();
    private final CallAdvisorChain chain = mock(CallAdvisorChain.class);
    private final ChatClientRequest request = new ChatClientRequest(new Prompt("Tere"), Map.of());

    /**
     * Advisor kutsub ahela edasi ja tagastab sama vastuse muutmata.
     */
    @Test
    void returnsSameResponseFromChain() {
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("Vastus"))));
        ChatClientResponse chatClientResponse = ChatClientResponse.builder().chatResponse(chatResponse).build();
        when(chain.nextCall(any())).thenReturn(chatClientResponse);

        ChatClientResponse result = aiMetricsAdvisor.adviseCall(request, chain);

        assertSame(chatClientResponse, result);
        verify(chain).nextCall(request);
    }

    /**
     * ChatResponse puudumine ei põhjusta viga.
     */
    @Test
    void doesNotFailWhenChatResponseMissing() {
        ChatClientResponse chatClientResponse = ChatClientResponse.builder().build();
        when(chain.nextCall(any())).thenReturn(chatClientResponse);

        assertSame(chatClientResponse, aiMetricsAdvisor.adviseCall(request, chain));
    }
}
