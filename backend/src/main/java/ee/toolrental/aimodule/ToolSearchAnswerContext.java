package ee.toolrental.aimodule;

record ToolSearchAnswerContext(String question, long totalToolCount, long availableToolCount,
                               String location, String category) {
}
