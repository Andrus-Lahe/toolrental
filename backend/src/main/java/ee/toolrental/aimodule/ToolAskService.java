package ee.toolrental.aimodule;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolAskService {

    private final ToolSearchQueryRepository queryRepository;
    private final ToolSearchMatcher searchMatcher;
    private final ToolSearchAnswerGenerator answerGenerator;

    public ToolAskService(ToolSearchQueryRepository queryRepository,
                          ToolSearchMatcher searchMatcher,
                          ToolSearchAnswerGenerator answerGenerator) {
        this.queryRepository = queryRepository;
        this.searchMatcher = searchMatcher;
        this.answerGenerator = answerGenerator;
    }

    public ToolAskResponse ask(String question) {
        List<ToolSearchCandidate> matches = queryRepository.findVisibleToolsByExactName(question.trim());
        if (matches.isEmpty()) {
            List<ToolSearchCandidate> candidates = queryRepository.findVisibleTools();
            List<Integer> matchingIds = searchMatcher.findMatchingToolIds(question, candidates);
            java.util.Set<Integer> selectedIds = java.util.Set.copyOf(matchingIds);
            matches = candidates.stream().filter(candidate -> selectedIds.contains(candidate.toolId())).toList();
        }

        ToolSearchSummary summary = ToolSearchSummary.fromCandidates(matches);
        String answer = answerGenerator.generateAnswer(summary.answerContext(question));
        return new ToolAskResponse(answer, summary.tools());
    }
}
