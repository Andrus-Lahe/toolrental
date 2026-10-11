package ee.toolrental.aimodule;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ToolAskService {

    private final ToolSearchQueryRepository repository;
    private final ToolSearchMatcher matcher;
    private final ToolSearchAnswerGenerator answerGenerator;
    private final SqlInjectionSurprise sqlReply;

    public ToolAskService(ToolSearchQueryRepository repository,
                          ToolSearchMatcher matcher,
                          ToolSearchAnswerGenerator answerGenerator,
                          SqlInjectionSurprise sqlReply) {
        this.repository = repository;
        this.matcher = matcher;
        this.answerGenerator = answerGenerator;
        this.sqlReply = sqlReply;
    }

    public ToolAskResponse ask(String question) {
        if (sqlReply.isSqlInjectionAttempt(question)) {
            return new ToolAskResponse(sqlReply.generateScolding(question), List.of());
        }

        List<ToolSearchCandidate> matches = repository.findVisibleToolsByExactName(question.trim());
        if (matches.isEmpty()) {
            List<ToolSearchCandidate> candidates = repository.findVisibleTools();
            ToolSearchMatchResult matchResult = matcher.match(question, candidates);
            if (!matchResult.toolSearchQuestion()) throw new UnsupportedToolSearchQuestionException();
            Set<Integer> selectedIds = Set.copyOf(matchResult.toolIds());
            matches = candidates.stream().filter(candidate -> selectedIds.contains(candidate.toolId())).toList();
        }

        ToolSearchSummary summary = ToolSearchSummary.fromCandidates(question, matches);
        String answer = answerGenerator.generateAnswer(summary);
        return new ToolAskResponse(answer, summary.tools());
    }
}
