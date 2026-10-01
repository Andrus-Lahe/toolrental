package ee.toolrental.persistence.tool;

import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.category.Category;
import ee.toolrental.persistence.toolimage.ToolImage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kasutab kohalikku vali_it PostgreSQL andmebaasi koos 3_import.sql algandmetega.
 * Iga test jookseb transaktsioonis, mis lõpus tagasi võetakse, seega andmebaasi andmed ei muutu.
 */
@DataJpaTest(properties = "spring.sql.init.mode=never")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ToolDeleteRepositoryTest {

    @Autowired
    private ToolDeleteRepository toolDeleteRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Impordiandmete tööriist 1 kuulub kasutajale 1 ja olematu tööriista omanik on tühi.
     */
    @Test
    void findsOwnerOfImportedToolAndNothingForMissingTool() {
        assertEquals(1, toolDeleteRepository.findOwnerIdBy(1).orElseThrow());
        assertTrue(toolDeleteRepository.findOwnerIdBy(999999).isEmpty());
    }

    /**
     * Impordi tööriistal 1 on broneeringuid, uuel tööriistal mitte.
     */
    @Test
    void importedToolHasBookingsAndNewToolDoesNot() {
        Tool newTool = persistTool("Broneeringuteta-testtööriist");

        assertTrue(toolDeleteRepository.existsBookingOfTool(1));
        assertFalse(toolDeleteRepository.existsBookingOfTool(newTool.getId()));
    }

    /**
     * Pildiga tööriista kustutamine: pilt ja tööriist kustuvad (1 rida kummastki), teised tööriistad jäävad.
     */
    @Test
    void deletesImageAndTool_othersRemain() {
        Tool tool = persistTool("Pildiga-testtööriist");
        ToolImage toolImage = new ToolImage();
        toolImage.setTool(tool);
        toolImage.setImageData(new byte[]{1, 2, 3});
        toolImage.setMain(true);
        entityManager.persist(toolImage);
        entityManager.flush();
        Integer toolId = tool.getId();

        assertEquals(1, toolDeleteRepository.deleteToolImagesOf(toolId));
        assertEquals(1, toolDeleteRepository.deleteToolBy(toolId));

        assertNull(entityManager.find(Tool.class, toolId));
        assertNotNull(entityManager.find(Tool.class, 1));
    }

    /**
     * Pildita tööriista kustutamine: piltide kustutamine annab 0 rida, tööriist kustub (1 rida).
     */
    @Test
    void toolWithoutImage_isDeleted() {
        Tool tool = persistTool("Pildita-testtööriist");
        Integer toolId = tool.getId();

        assertEquals(0, toolDeleteRepository.deleteToolImagesOf(toolId));
        assertEquals(1, toolDeleteRepository.deleteToolBy(toolId));

        assertNull(entityManager.find(Tool.class, toolId));
    }

    /**
     * Loob ja salvestab kasutajale 5 kuuluva testtööriista (transaktsioon võetakse testi lõpus tagasi).
     */
    private Tool persistTool(String name) {
        Tool tool = new Tool();
        tool.setOwner(entityManager.find(AppUser.class, 5));
        tool.setCategory(entityManager.find(Category.class, 1));
        tool.setName(name);
        tool.setStatus("A");
        tool.setCreatedAt(Instant.now());
        tool.setUpdatedAt(Instant.now());
        entityManager.persist(tool);
        entityManager.flush();
        return tool;
    }
}
