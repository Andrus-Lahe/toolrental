package ee.toolrental.persistence.toolimage;

import ee.toolrental.persistence.appuser.AppUserRepository;
import ee.toolrental.persistence.category.CategoryRepository;
import ee.toolrental.persistence.tool.Tool;
import ee.toolrental.persistence.tool.ToolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ToolImageRepositoryTest {

    @Autowired
    private ToolImageRepository toolImageRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findsMainImageOfImportedTool() {
        Optional<ToolImage> toolImage = toolImageRepository.findMainToolImageBy(1);

        assertTrue(toolImage.isPresent());
        assertEquals(1, toolImage.get().getId());
        assertTrue(toolImage.get().getIsMain());
    }

    @Test
    void toolWithOnlySecondaryImageHasNoMainImage() {
        Tool tool = saveTool("Lisapildiga tööriist");
        saveToolImage(tool, "<svg>lisapilt</svg>", false);

        assertTrue(toolImageRepository.findMainToolImageBy(tool.getId()).isEmpty());
    }

    @Test
    void toolWithoutImagesHasNoMainImage() {
        Tool tool = saveTool("Pildita tööriist");

        assertTrue(toolImageRepository.findMainToolImageBy(tool.getId()).isEmpty());
    }

    private void saveToolImage(Tool tool, String content, Boolean isMain) {
        ToolImage toolImage = new ToolImage();
        toolImage.setTool(tool);
        toolImage.setImageData(content.getBytes(StandardCharsets.UTF_8));
        toolImage.setIsMain(isMain);
        toolImageRepository.saveAndFlush(toolImage);
    }

    private Tool saveTool(String name) {
        Tool tool = new Tool();
        tool.setOwner(appUserRepository.findById(1).orElseThrow());
        tool.setCategory(categoryRepository.findById(1).orElseThrow());
        tool.setName(name);
        tool.setStatus("A");
        tool.setCreatedAt(Instant.now());
        tool.setUpdatedAt(Instant.now());
        return toolRepository.saveAndFlush(tool);
    }
}
