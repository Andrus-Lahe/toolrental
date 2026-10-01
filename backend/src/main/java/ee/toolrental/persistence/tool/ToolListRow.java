package ee.toolrental.persistence.tool;

/**
 * Tööriistade nimekirja päringu rida: tööriista andmed, põhipilt ja omaniku aadressi linn/linnaosa.
 * Pilt, linn ja linnaosa võivad olla NULL (põhipilt või omaniku profiil puudub).
 */
public record ToolListRow(
        Integer toolId,
        String toolName,
        String description,
        byte[] imageData,
        String status,
        String cityName,
        String districtName
) {
}
