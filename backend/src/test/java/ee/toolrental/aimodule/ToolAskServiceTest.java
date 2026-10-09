package ee.toolrental.aimodule;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class ToolAskServiceTest {

    private static final String QUESTION = "Millised tööriistad on saadaval Kristiines?";
    private static final String VALID_SQL = "SELECT tool_id, tool_name, category_name, location_name " +
            "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
            "AND location_name ILIKE '%Kristiine%' ORDER BY tool_id LIMIT 20";

    @Test
    void generatesValidatesAndExecutesSqlInOrder() {
        ToolSearchSqlGenerator generator = mock(ToolSearchSqlGenerator.class);
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        ToolSearchAnswerGenerator answerGenerator = mock(ToolSearchAnswerGenerator.class);
        when(generator.generateSql(QUESTION)).thenReturn(VALID_SQL);
        AvailableTool tool = new AvailableTool(12, "Akutrell", "Ehitustööd", "Kristiine");
        when(repository.searchWithCounts(any())).thenReturn(
                new ToolSearchQueryRepository.SearchResults(List.of(tool), 3, 2));
        when(answerGenerator.generateAnswer(any())).thenReturn("Kristiines leidub 3 tööriista; 2 on saadaval.");

        ToolAskResponse response = new ToolAskService(generator, new ToolSearchSqlValidator(), repository,
                answerGenerator).ask(QUESTION);

        assertThat(response.tools()).containsExactly(tool);
        assertThat(response.answer()).contains("3", "2");
        var order = inOrder(generator, repository, answerGenerator);
        order.verify(generator).generateSql(QUESTION);
        order.verify(repository).searchWithCounts(any());
        order.verify(answerGenerator).generateAnswer(new ToolSearchAnswerContext(QUESTION, 3, 2,
                "Kristiine", null));
    }

    @Test
    void doesNotValidateOrExecuteUnsupportedQuestionMarker() {
        ToolSearchSqlGenerator generator = mock(ToolSearchSqlGenerator.class);
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        ToolSearchAnswerGenerator answerGenerator = mock(ToolSearchAnswerGenerator.class);
        when(generator.generateSql(QUESTION)).thenReturn(ToolSearchSqlGenerator.CANNOT_ANSWER);

        assertThatThrownBy(() -> new ToolAskService(generator, new ToolSearchSqlValidator(), repository,
                answerGenerator).ask(QUESTION))
                .isInstanceOf(UnsupportedToolSearchQuestionException.class);
        verify(repository, never()).searchWithCounts(any());
        verifyNoInteractions(answerGenerator);
    }

    @Test
    void rejectsInvalidGeneratedSqlBeforeRepositoryExecution() {
        ToolSearchSqlGenerator generator = mock(ToolSearchSqlGenerator.class);
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        ToolSearchAnswerGenerator answerGenerator = mock(ToolSearchAnswerGenerator.class);
        when(generator.generateSql(QUESTION)).thenReturn(VALID_SQL.replace("ai_tool_search", "tool"));
        ToolAskService service = new ToolAskService(generator, new ToolSearchSqlValidator(), repository,
                answerGenerator);

        assertThatThrownBy(() -> service.ask(QUESTION))
                .isInstanceOf(InvalidToolSearchSqlException.class);
        verify(repository, never()).searchWithCounts(any());
        verifyNoInteractions(answerGenerator);
    }
}
