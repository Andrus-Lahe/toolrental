package ee.toolrental.aimodule;

import java.util.List;
import java.util.Objects;

record ToolSearchSummary(List<AvailableTool> tools, long totalToolCount, long availableToolCount,
                         String location, String category) {
    ToolSearchSummary {
        tools = List.copyOf(tools);
    }

    static ToolSearchSummary fromCandidates(List<ToolSearchCandidate> matches) {
        List<AvailableTool> availableTools = matches.stream()
                .filter(ToolSearchCandidate::availableNow)
                .map(ToolSearchCandidate::toAvailableTool)
                .toList();
        return new ToolSearchSummary(availableTools, matches.size(), availableTools.size(),
                uniqueCandidateValue(matches, ToolSearchCandidate::location),
                uniqueCandidateValue(matches, ToolSearchCandidate::category));
    }

    ToolSearchAnswerContext answerContext(String question) {
        return new ToolSearchAnswerContext(question, totalToolCount, availableToolCount, location, category);
    }

    private static String uniqueCandidateValue(List<ToolSearchCandidate> candidates,
                                               java.util.function.Function<ToolSearchCandidate, String> value) {
        List<String> values = candidates.stream().map(value).filter(Objects::nonNull)
                .map(String::trim).filter(candidate -> !candidate.isBlank()).distinct().toList();
        return values.size() == 1 ? values.getFirst() : null;
    }
}
