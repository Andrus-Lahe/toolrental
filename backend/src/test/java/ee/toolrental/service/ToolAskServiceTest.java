package ee.toolrental.service;

import ee.toolrental.controller.ai.ToolAskResponse;
import ee.toolrental.persistence.ai.AvailableTool;
import ee.toolrental.persistence.ai.AvailableToolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolAskServiceTest {

    @Test
    void kristiineQuestionUsesOnlyAvailableToolsInKristiine() {
        AvailableToolRepository repository = mock(AvailableToolRepository.class);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(builder.build()).thenReturn(chatClient);
        when(repository.cityNames()).thenReturn(List.of("Tallinn"));
        when(repository.districtNames()).thenReturn(List.of("Kristiine", "Lasnamäe"));
        when(repository.categoryNames()).thenReturn(List.of("Puurid"));
        when(chatClient.prompt().system(anyString()).user(anyString()).call()
                .entity(ToolSearchIntent.class))
                .thenReturn(new ToolSearchIntent(true, null, "Kristiines", null));
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("Kristiines on saadaval Akutrell.");

        AvailableTool tool = new AvailableTool(1, "Akutrell", "Puurid", "Tallinn", "Kristiine");
        when(repository.findAvailable(null, "Kristiine", null)).thenReturn(List.of(tool));

        ToolAskResponse response = new ToolAskService(repository, builder)
                .ask("Millised tööriistad on saadaval Kristiines?");

        assertThat(response.answer()).isEqualTo("Kristiines on saadaval Akutrell.");
        assertThat(response.tools()).containsExactly(tool);
        verify(repository).findAvailable(null, "Kristiine", null);
    }
}
