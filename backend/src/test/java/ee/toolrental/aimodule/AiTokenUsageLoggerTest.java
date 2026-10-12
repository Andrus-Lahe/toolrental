package ee.toolrental.aimodule;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.metadata.GoogleGenAiUsage;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiTokenUsageLoggerTest {

    private final AiTokenUsageLogger tokenLogger = new AiTokenUsageLogger(
            new BigDecimal("0.30"), new BigDecimal("2.50"), new BigDecimal("0.03"), new BigDecimal("1.25"));
    private final Logger logger = (Logger) LoggerFactory.getLogger(AiTokenUsageLogger.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void captureLogs() {
        appender.list = Collections.synchronizedList(new ArrayList<>());
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void autoConfiguredClientsLogExactlyFiveLinesIncludingThinkingAndCachedInput() {
        ChatModel model = mock(ChatModel.class);
        when(model.getOptions()).thenReturn(ChatOptions.builder().model("gemini-3.5-flash-lite").build());
        when(model.call(any(Prompt.class))).thenReturn(
                response(1200, 35, 25, 200), response(250, 60, 0, 0));

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ChatClientAutoConfiguration.class))
                .withBean(ChatModel.class, () -> model)
                .withBean(AiTokenUsageLogger.class, () -> tokenLogger)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    ChatClient first = context.getBean(ChatClient.Builder.class).build();
                    ChatClient second = context.getBean(ChatClient.Builder.class).build();
                    String result = tokenLogger.logRequest(() -> {
                        assertThat(first.prompt("select tools").call().content()).isEqualTo("answer");
                        tokenLogger.secondRequest();
                        return second.prompt("generate answer").call().content();
                    });
                    assertThat(result).isEqualTo("answer");
                });

        verify(model, times(2)).call(any(Prompt.class));
        assertThat(messages()).containsExactly("""

                First request Output token: 60 0.00012000 €
                First request Input token: 1200 0.00024480 €
                Second request Output token: 60 0.00012000 €
                Second request Input token: 250 0.00006000 €
                Total: 1570 0.00054480 €""");
    }

    @Test
    void skippedFirstStageHasZeroCostAndAnswerStaysInSecondStage() {
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        ToolSearchMatcher matcher = mock(ToolSearchMatcher.class);
        ToolSearchAnswerGenerator answerGenerator = mock(ToolSearchAnswerGenerator.class);
        SqlInjectionSurprise sqlReply = mock(SqlInjectionSurprise.class);
        ToolSearchCandidate candidate = new ToolSearchCandidate(1, "Labidas", "Kaevamine",
                "Aiatööd", "Kristiine", "Tallinn", true);
        when(repository.findVisibleToolsByExactName("Labidas")).thenReturn(List.of(candidate));
        when(answerGenerator.generateAnswer(any())).thenAnswer(invocation -> {
            record(response(10, 2, 0, 0));
            return "Labidas on saadaval.";
        });

        var service = new ToolAskService(repository, matcher, answerGenerator, sqlReply, tokenLogger);
        assertThat(service.ask("Labidas").answer()).isEqualTo("Labidas on saadaval.");

        assertThat(messages()).containsExactly("""

                First request Output token: 0 0.00000000 €
                First request Input token: 0 0.00000000 €
                Second request Output token: 2 0.00000400 €
                Second request Input token: 10 0.00000240 €
                Total: 12 0.00000640 €""");
    }

    @Test
    void failurePreservesExceptionLogsUnknownCostAndClearsCountersForNextQuestion() {
        var failure = new IllegalStateException("provider unavailable");
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        when(chain.nextCall(any())).thenThrow(failure);

        assertThatThrownBy(() -> tokenLogger.logRequest(() -> {
            record(response(10, 2, 0, 0));
            tokenLogger.secondRequest();
            return tokenLogger.adviseCall(null, chain);
        })).isSameAs(failure);
        tokenLogger.logRequest(() -> record(response(20, 3, 0, 0)));

        assertThat(messages()).containsExactly("""

                First request Output token: 2 0.00000400 €
                First request Input token: 10 0.00000240 €
                Second request Output token: N/A N/A
                Second request Input token: N/A N/A
                Total: N/A N/A""", """

                First request Output token: 3 0.00000600 €
                First request Input token: 20 0.00000480 €
                Second request Output token: 0 0.00000000 €
                Second request Input token: 0 0.00000000 €
                Total: 23 0.00001080 €""");
    }

    @Test
    void absentProviderUsageIsUnknownRatherThanFree() {
        ChatResponse noUsage = new ChatResponse(List.of(new Generation(new AssistantMessage("answer"))),
                ChatResponseMetadata.builder().usage(GoogleGenAiUsage.from(null)).build());
        tokenLogger.logRequest(() -> record(noUsage));

        assertThat(messages()).containsExactly("""

                First request Output token: N/A N/A
                First request Input token: N/A N/A
                Second request Output token: 0 0.00000000 €
                Second request Input token: 0 0.00000000 €
                Total: N/A N/A""");
    }

    @Test
    void freeTierConfigurationKeepsTokenCountsWithZeroPrices() {
        var freeLogger = new AiTokenUsageLogger(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        when(chain.nextCall(any())).thenReturn(new ChatClientResponse(response(10, 2, 3, 0), Map.of()));
        freeLogger.logRequest(() -> freeLogger.adviseCall(null, chain));

        assertThat(messages()).containsExactly("""

                First request Output token: 5 0.00000000 €
                First request Input token: 10 0.00000000 €
                Second request Output token: 0 0.00000000 €
                Second request Input token: 0 0.00000000 €
                Total: 15 0.00000000 €""");
    }

    @Test
    void concurrentQuestionsHaveIndependentFiveLineBlocks() throws Exception {
        CountDownLatch bothStarted = new CountDownLatch(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> tokenLogger.logRequest(() -> {
                record(response(10, 2, 0, 0));
                awaitBoth(bothStarted);
                return "first";
            }));
            var second = executor.submit(() -> tokenLogger.logRequest(() -> {
                record(response(20, 3, 0, 0));
                awaitBoth(bothStarted);
                tokenLogger.secondRequest();
                record(response(5, 1, 0, 0));
                return "second";
            }));
            assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo("first");
            assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo("second");
        }

        assertThat(messages()).containsExactlyInAnyOrder("""

                First request Output token: 2 0.00000400 €
                First request Input token: 10 0.00000240 €
                Second request Output token: 0 0.00000000 €
                Second request Input token: 0 0.00000000 €
                Total: 12 0.00000640 €""", """

                First request Output token: 3 0.00000600 €
                First request Input token: 20 0.00000480 €
                Second request Output token: 1 0.00000200 €
                Second request Input token: 5 0.00000120 €
                Total: 29 0.00001400 €""");
    }

    private ChatClientResponse record(ChatResponse response) {
        var result = new ChatClientResponse(response, Map.of());
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        when(chain.nextCall(any())).thenReturn(result);
        assertThat(tokenLogger.adviseCall(null, chain)).isSameAs(result);
        return result;
    }

    private static ChatResponse response(int input, int output, int thinking, int cached) {
        var nativeUsage = GenerateContentResponseUsageMetadata.builder()
                .promptTokenCount(input).candidatesTokenCount(output).thoughtsTokenCount(thinking)
                .cachedContentTokenCount(cached).totalTokenCount(input + output + thinking).build();
        return new ChatResponse(List.of(new Generation(new AssistantMessage("answer"))),
                ChatResponseMetadata.builder().usage(GoogleGenAiUsage.from(nativeUsage)).build());
    }

    private List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    private static void awaitBoth(CountDownLatch latch) {
        latch.countDown();
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) throw new IllegalStateException("Parallel question did not start");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
