package ee.toolrental.persistence.tool;

import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserRepository;
import ee.toolrental.persistence.category.CategoryRepository;
import ee.toolrental.persistence.role.RoleRepository;
import ee.toolrental.persistence.toolimage.ToolImage;
import ee.toolrental.persistence.toolimage.ToolImageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ToolRepositoryTest {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolImageRepository toolImageRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void defaultQueryReturnsAvailableToolsOrderedById() {
        Page<ToolListRow> toolListRowPage = findToolListRowPage(0, 0, 0, "A", 0, 12);

        assertEquals(List.of(1, 3, 4, 5, 6, 7, 8), toolIdsOf(toolListRowPage));
        assertEquals(7L, toolListRowPage.getTotalElements());
        assertEquals(1, toolListRowPage.getTotalPages());
    }

    @Test
    void controlTableOfImportDataMatches() {
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), toolIdsOf(findToolListRowPage(0, 0, 0, "0", 0, 12)));
        assertEquals(List.of(2), toolIdsOf(findToolListRowPage(0, 0, 0, "U", 0, 12)));
        assertEquals(List.of(1), toolIdsOf(findToolListRowPage(2, 0, 0, "A", 0, 12)));
        assertEquals(List.of(3, 4, 8), toolIdsOf(findToolListRowPage(0, 1, 2, "A", 0, 12)));

        Page<ToolListRow> emptyToolListRowPage = findToolListRowPage(0, 2, 0, "0", 0, 12);
        assertTrue(emptyToolListRowPage.getContent().isEmpty());
        assertEquals(0L, emptyToolListRowPage.getTotalElements());
        assertEquals(0, emptyToolListRowPage.getTotalPages());
    }

    @Test
    void conflictingLocationFiltersAndMissingIdProduceEmptyResult() {
        assertTrue(findToolListRowPage(0, 2, 1, "0", 0, 12).getContent().isEmpty());
        assertTrue(findToolListRowPage(999, 0, 0, "0", 0, 12).getContent().isEmpty());
    }

    @Test
    void smallPageSizeSplitsResultIntoPages() {
        Page<ToolListRow> firstToolListRowPage = findToolListRowPage(0, 0, 0, "A", 0, 6);
        Page<ToolListRow> secondToolListRowPage = findToolListRowPage(0, 0, 0, "A", 1, 6);

        assertEquals(List.of(1, 3, 4, 5, 6, 7), toolIdsOf(firstToolListRowPage));
        assertEquals(List.of(8), toolIdsOf(secondToolListRowPage));
        assertEquals(2, secondToolListRowPage.getTotalPages());
        assertEquals(7L, secondToolListRowPage.getTotalElements());
    }

    @Test
    void pageAfterLastPageKeepsTotals() {
        Page<ToolListRow> toolListRowPage = findToolListRowPage(0, 0, 0, "A", 1, 12);

        assertTrue(toolListRowPage.getContent().isEmpty());
        assertEquals(7L, toolListRowPage.getTotalElements());
        assertEquals(1, toolListRowPage.getTotalPages());
    }

    @Test
    void returnsLocationNamesAndMainImageBytes() {
        ToolListRow toolListRow = findToolListRowPage(2, 0, 0, "A", 0, 12).getContent().getFirst();
        byte[] mainImageData = toolImageRepository.findById(1).orElseThrow().getImageData();

        assertEquals("Akutrell", toolListRow.toolName());
        assertEquals("Tallinn", toolListRow.cityName());
        assertEquals("Kristiine", toolListRow.districtName());
        assertArrayEquals(mainImageData, toolListRow.imageData());
    }

    @Test
    void additionalImageDoesNotDuplicateToolOrReplaceMainImage() {
        Tool akutrell = toolRepository.findById(1).orElseThrow();
        byte[] mainImageData = toolImageRepository.findById(1).orElseThrow().getImageData();
        saveToolImage(akutrell, "lisapilt", false);

        Page<ToolListRow> toolListRowPage = findToolListRowPage(0, 0, 0, "A", 0, 12);

        assertEquals(List.of(1, 3, 4, 5, 6, 7, 8), toolIdsOf(toolListRowPage));
        assertEquals(7L, toolListRowPage.getTotalElements());
        assertArrayEquals(mainImageData, toolListRowPage.getContent().getFirst().imageData());
    }

    @Test
    void toolWithOnlyAdditionalImageHasNullImageData() {
        Tool tool = saveTool(appUserRepository.findById(1).orElseThrow(), "Ainult lisapildiga", "kirjeldus");
        saveToolImage(tool, "lisapilt", false);

        ToolListRow toolListRow = findRowOf(tool.getId(), findToolListRowPage(0, 0, 0, "A", 0, 12));

        assertNull(toolListRow.imageData());
    }

    @Test
    void ownerWithoutProfileKeepsToolWithoutLocationFilter() {
        AppUser ownerWithoutProfile = new AppUser();
        ownerWithoutProfile.setRole(roleRepository.findById(2).orElseThrow());
        ownerWithoutProfile.setFirstName("Profiilita");
        ownerWithoutProfile.setLastName("Omanik");
        ownerWithoutProfile.setGoogleSub("test-profiilita-omanik");
        ownerWithoutProfile.setStatus("A");
        appUserRepository.saveAndFlush(ownerWithoutProfile);
        Tool tool = saveTool(ownerWithoutProfile, "Profiilita omaniku tööriist", null);

        Page<ToolListRow> toolListRowPage = findToolListRowPage(0, 0, 0, "A", 0, 12);
        ToolListRow toolListRow = findRowOf(tool.getId(), toolListRowPage);

        assertEquals(8L, toolListRowPage.getTotalElements());
        assertNull(toolListRow.description());
        assertNull(toolListRow.cityName());
        assertNull(toolListRow.districtName());
        assertTrue(toolIdsOf(findToolListRowPage(0, 1, 0, "A", 0, 12)).stream()
                .noneMatch(toolId -> toolId.equals(tool.getId())));
    }

    private Page<ToolListRow> findToolListRowPage(Integer categoryId, Integer cityId, Integer districtId,
                                                  String status, int page, int size) {
        return toolRepository.findToolListRowsBy(categoryId, cityId, districtId, status, PageRequest.of(page, size));
    }

    private List<Integer> toolIdsOf(Page<ToolListRow> toolListRowPage) {
        return toolListRowPage.getContent().stream()
                .map(ToolListRow::toolId)
                .toList();
    }

    private ToolListRow findRowOf(Integer toolId, Page<ToolListRow> toolListRowPage) {
        return toolListRowPage.getContent().stream()
                .filter(toolListRow -> toolListRow.toolId().equals(toolId))
                .findFirst()
                .orElseThrow();
    }

    private Tool saveTool(AppUser owner, String name, String description) {
        Tool tool = new Tool();
        tool.setOwner(owner);
        tool.setCategory(categoryRepository.findById(1).orElseThrow());
        tool.setName(name);
        tool.setDescription(description);
        tool.setStatus("A");
        tool.setCreatedAt(Instant.now());
        tool.setUpdatedAt(Instant.now());
        return toolRepository.saveAndFlush(tool);
    }

    private void saveToolImage(Tool tool, String content, Boolean isMain) {
        ToolImage toolImage = new ToolImage();
        toolImage.setTool(tool);
        toolImage.setImageData(content.getBytes(StandardCharsets.UTF_8));
        toolImage.setIsMain(isMain);
        toolImageRepository.saveAndFlush(toolImage);
    }
}
