package ee.toolrental.aimodule;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolSearchSqlGeneratorTest {

    @Test
    void generatesRawSqlForOnlyTheRestrictedViewAndPassesQuestionUnchanged() {
        String question = "Millised tööriistad on saadaval Kristiines?";
        String generatedSql = "SELECT tool_id, tool_name, category_name, location_name " +
                "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
                "AND location_name ILIKE '%Kristiine%' ORDER BY tool_id LIMIT 20";
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt().system(anyString()).user(question).call().content())
                .thenReturn("\n" + generatedSql + "\n");

        String result = new ToolSearchSqlGenerator(chatClientBuilder).generateSql(question);

        assertThat(result).isEqualTo(generatedSql);
        verify(chatClient.prompt().system(anyString()), atLeastOnce()).user(question);
    }

    @Test
    void promptRestrictsModelContextToTheAllowedViewAndAvailabilityField() {
        String question = "Millised tööriistad on saadaval Kristiines?";
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt().system(anyString()).user(question).call().content())
                .thenReturn("CANNOT_ANSWER");

        new ToolSearchSqlGenerator(chatClientBuilder).generateSql(question);

        ArgumentCaptor<String> systemPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatClient.prompt(), atLeastOnce()).system(systemPromptCaptor.capture());
        assertThat(systemPromptCaptor.getValue())
                .contains("tool_rental.ai_tool_search", "is_available_now = TRUE", "tool_id",
                        "tool_name", "category_name", "location_name", "CANNOT_ANSWER")
                .doesNotContain("google_sub", "email", "profile");
    }

    @Test
    void passesUnsupportedQuestionMarkerThroughForAi05ToHandle() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("  CANNOT_ANSWER\n");

        String result = new ToolSearchSqlGenerator(chatClientBuilder).generateSql("Kui palju maksab laenutus?");

        assertThat(result).isEqualTo(ToolSearchSqlGenerator.CANNOT_ANSWER);
    }

    @Test
    void rejectsEmptyModelOutput() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(" \n ");

        assertThatThrownBy(() -> new ToolSearchSqlGenerator(chatClientBuilder)
                .generateSql("Millised tööriistad on saadaval Kristiines?"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini returned an empty SQL query");
    }
}
