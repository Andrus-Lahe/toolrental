package ee.toolrental.aimodule;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolAskServiceTest {

    private static final String QUESTION = "mul muru pikk";

    @Test
    void selectsMatchingDatabaseCandidatesAndAnswersWithTheirCounts() {
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        ToolSearchMatcher matcher = mock(ToolSearchMatcher.class);
        ToolSearchAnswerGenerator answerGenerator = mock(ToolSearchAnswerGenerator.class);
        ToolSearchCandidate mower = new ToolSearchCandidate(12, "Muruniiduk", "Muru niitmiseks",
                "Aiatööd", "Kristiine", "Tallinn", true);
        ToolSearchCandidate trimmer = new ToolSearchCandidate(13, "Trimmer", "Muru servade pügamiseks",
                "Aiatööd", "Kristiine", "Tallinn", false);
        when(repository.findVisibleToolsByExactName(QUESTION)).thenReturn(List.of());
        when(repository.findVisibleTools()).thenReturn(List.of(mower, trimmer));
        when(matcher.match(QUESTION, List.of(mower, trimmer)))
                .thenReturn(new ToolSearchMatchResult(true, List.of(12, 13, 999)));
        when(answerGenerator.generateAnswer(new ToolSearchAnswerContext(QUESTION, 2, 1,
                "Kristiine", "Aiatööd"))).thenReturn("Leidsin 2 sobivat tööriista; 1 on saadaval.");

        ToolAskResponse response = new ToolAskService(repository, matcher, answerGenerator).ask(QUESTION);

        assertThat(response.tools()).containsExactly(mower.toAvailableTool());
        assertThat(response.answer()).contains("2", "1");
        var order = inOrder(repository, matcher, answerGenerator);
        order.verify(repository).findVisibleToolsByExactName(QUESTION);
        order.verify(repository).findVisibleTools();
        order.verify(matcher).match(QUESTION, List.of(mower, trimmer));
        order.verify(answerGenerator).generateAnswer(new ToolSearchAnswerContext(QUESTION, 2, 1,
                "Kristiine", "Aiatööd"));
    }

    @Test
    void exactToolNameSearchSkipsAiMatching() {
        ToolSearchQueryRepository repository = mock(ToolSearchQueryRepository.class);
        ToolSearchMatcher matcher = mock(ToolSearchMatcher.class);
        ToolSearchAnswerGenerator answerGenerator = mock(ToolSearchAnswerGenerator.class);
        String toolName = "Muruniiduk";
        ToolSearchCandidate mower = new ToolSearchCandidate(12, toolName, "Muru niitmiseks",
                "Aiatööd", "Kristiine", "Tallinn", true);
        when(repository.findVisibleToolsByExactName(toolName)).thenReturn(List.of(mower));
        when(answerGenerator.generateAnswer(new ToolSearchAnswerContext(toolName, 1, 1,
                "Kristiine", "Aiatööd"))).thenReturn("Muruniiduk on saadaval.");

        ToolAskResponse response = new ToolAskService(repository, matcher, answerGenerator).ask(toolName);

        assertThat(response.tools()).containsExactly(mower.toAvailableTool());
        verify(repository, never()).findVisibleTools();
        verify(matcher, never()).match(org.mockito.ArgumentMatchers.anyString(), anyList());
    }
}
