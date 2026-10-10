package ee.toolrental.aimodule;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolAskService {

    private final ToolSearchQueryRepository queryRepository;
    private final ToolSearchMatcher searchMatcher;
    private final ToolSearchAnswerGenerator answerGenerator;
    private final SqlInjectionSurprise sqlInjectionSurprise;

    public ToolAskService(ToolSearchQueryRepository queryRepository,
                          ToolSearchMatcher searchMatcher,
                          ToolSearchAnswerGenerator answerGenerator,
                          SqlInjectionSurprise sqlInjectionSurprise) {
        this.queryRepository = queryRepository;
        this.searchMatcher = searchMatcher;
        this.answerGenerator = answerGenerator;
        this.sqlInjectionSurprise = sqlInjectionSurprise;
    }

    public ToolAskResponse ask(String question) {
        if (sqlInjectionSurprise.isSqlInjectionAttempt(question)) {
            return new ToolAskResponse(sqlInjectionSurprise.generateScolding(question), List.of());
        }

        List<ToolSearchCandidate> matches = queryRepository.findVisibleToolsByExactName(question.trim());
        if (matches.isEmpty()) {
            List<ToolSearchCandidate> candidates = queryRepository.findVisibleTools();
            ToolSearchMatchResult matchResult = searchMatcher.match(question, candidates);
            if (!matchResult.toolSearchQuestion()) throw new UnsupportedToolSearchQuestionException();
            java.util.Set<Integer> selectedIds = java.util.Set.copyOf(matchResult.toolIds());
            matches = candidates.stream().filter(candidate -> selectedIds.contains(candidate.toolId())).toList();
        }

        ToolSearchSummary summary = ToolSearchSummary.fromCandidates(matches);
        String answer = answerGenerator.generateAnswer(summary.answerContext(question));
        return new ToolAskResponse(answer, summary.tools());
    }
}
