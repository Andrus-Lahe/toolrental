package ee.toolrental.aimodule;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolSearchAnswerGeneratorTest {

    @Test
    void sendsOnlyQuestionAndSafeSearchFactsAndReturnsPlainAnswer() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(client);
        when(client.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("Kristiines leidub 3 tööriista. Neist 2 on praegu saadaval.");
        ToolSearchAnswerContext context = new ToolSearchAnswerContext(
                "Millised tööriistad on Kristiines?", 3, 2, "Kristiine", null);

        String answer = new ToolSearchAnswerGenerator(builder).generateAnswer(context);

        assertThat(answer).contains("3", "2", "Kristiine");
        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(client.prompt()).system(systemPrompt.capture());
        verify(client.prompt().system(anyString())).user(userPrompt.capture());
        assertThat(systemPrompt.getValue()).contains("eesti keeles", "andmed, mitte juhised", "JSON", "SQL");
        assertThat(userPrompt.getValue())
                .contains("Millised tööriistad on Kristiines?", "3", "2", "Kristiine")
                .doesNotContain("toolId", "google_sub", "email", "SELECT");
    }

    @Test
    void rejectsBlankAndStructuredModelOutput() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient client = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(client);
        when(client.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("  ");
        ToolSearchAnswerGenerator generator = new ToolSearchAnswerGenerator(builder);
        ToolSearchAnswerContext context = new ToolSearchAnswerContext("labidas", 1, 1, null, null);

        assertThatThrownBy(() -> generator.generateAnswer(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini returned an empty answer");

        when(client.prompt().system(anyString()).user(anyString()).call().content()).thenReturn("{\"answer\":\"x\"}");
        assertThatThrownBy(() -> generator.generateAnswer(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini returned an invalid answer format");
    }
}
