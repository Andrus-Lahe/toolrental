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

class ToolAskServiceTest {

    private static final String QUESTION = "Millised tööriistad on saadaval Kristiines?";
    private static final String VALID_SQL = "SELECT tool_id, tool_name, category_name, location_name " +
            "FROM tool_rental.ai_tool_search WHERE is_available_now = TRUE " +
            "AND location_name ILIKE '%Kristiine%' ORDER BY tool_id LIMIT 20";

    @Test
    void generatesValidatesAndExecutesSqlInOrder() {
        ToolSearchSqlGenerator generator = mock(ToolSearchSqlGenerator.class);
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        when(generator.generateSql(QUESTION)).thenReturn(VALID_SQL);
        AvailableTool tool = new AvailableTool(12, "Akutrell", "Ehitustööd", null, "Kristiine");
        when(repository.search(any())).thenReturn(List.of(tool));

        ToolAskResponse response = new ToolAskService(generator, new ToolSearchSqlValidator(), repository).ask(QUESTION);

        assertThat(response.answer()).isEqualTo("Leidsin 1 saadaval tööriista.");
        assertThat(response.tools()).containsExactly(tool);
        var order = inOrder(generator, repository);
        order.verify(generator).generateSql(QUESTION);
        order.verify(repository).search(any());
    }

    @Test
    void doesNotValidateOrExecuteUnsupportedQuestionMarker() {
        ToolSearchSqlGenerator generator = mock(ToolSearchSqlGenerator.class);
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        when(generator.generateSql(QUESTION)).thenReturn(ToolSearchSqlGenerator.CANNOT_ANSWER);

        ToolAskResponse response = new ToolAskService(generator, new ToolSearchSqlValidator(), repository).ask(QUESTION);

        assertThat(response.answer()).isEqualTo("Saan aidata ainult saadaval tööriistade otsimisega.");
        assertThat(response.tools()).isEmpty();
        verify(repository, never()).search(any());
    }

    @Test
    void rejectsInvalidGeneratedSqlBeforeRepositoryExecution() {
        ToolSearchSqlGenerator generator = mock(ToolSearchSqlGenerator.class);
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        when(generator.generateSql(QUESTION)).thenReturn(VALID_SQL.replace("ai_tool_search", "tool"));
        ToolAskService service = new ToolAskService(generator, new ToolSearchSqlValidator(), repository);

        assertThatThrownBy(() -> service.ask(QUESTION))
                .isInstanceOf(InvalidToolSearchSqlException.class);
        verify(repository, never()).search(any());
    }
}
