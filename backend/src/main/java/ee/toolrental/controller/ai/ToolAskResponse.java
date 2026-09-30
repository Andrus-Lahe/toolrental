package ee.toolrental.controller.ai;

import ee.toolrental.persistence.ai.AvailableTool;

import java.util.List;

public record ToolAskResponse(String answer, List<AvailableTool> tools) {
}
