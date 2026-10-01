package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.tool.ToolDeleteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolDeleteServiceTest {

    private final ToolDeleteRepository toolDeleteRepository = mock(ToolDeleteRepository.class);
    private final ToolDeleteService toolDeleteService = new ToolDeleteService(toolDeleteRepository);

    /**
     * Omaniku tööriist ilma broneeringuteta kustutatakse: kõigepealt pildid, seejärel tööriist.
     */
    @Test
    void ownTool_withoutBookings_deletesImagesBeforeTool() {
        when(toolDeleteRepository.findOwnerIdBy(9)).thenReturn(Optional.of(5));
        when(toolDeleteRepository.existsBookingOfTool(9)).thenReturn(false);

        toolDeleteService.deleteTool(5, 9);

        InOrder inOrder = inOrder(toolDeleteRepository);
        inOrder.verify(toolDeleteRepository).deleteToolImagesOf(9);
        inOrder.verify(toolDeleteRepository).deleteToolBy(9);
    }

    /**
     * Olematu tööriist annab 404 PRIMARY_KEY_NOT_FOUND tegeliku ID-ga ja midagi ei kustutata.
     */
    @Test
    void missingTool_producesPrimaryKeyNotFound() {
        when(toolDeleteRepository.findOwnerIdBy(123)).thenReturn(Optional.empty());

        PrimaryKeyNotFoundException exception = assertThrows(
                PrimaryKeyNotFoundException.class, () -> toolDeleteService.deleteTool(5, 123));

        assertEquals("Ei leidnud primary keyd 'toolId' väärtusega: 123", exception.getMessage());
        verify(toolDeleteRepository, never()).deleteToolBy(123);
    }

    /**
     * Teise kasutaja tööriista kustutamine annab 403 TOOL_NOT_OWNED ja midagi ei kustutata.
     */
    @Test
    void toolOfOtherUser_producesToolNotOwned() {
        when(toolDeleteRepository.findOwnerIdBy(9)).thenReturn(Optional.of(3));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class, () -> toolDeleteService.deleteTool(5, 9));

        assertEquals("TOOL_NOT_OWNED", exception.getErrorCode());
        assertEquals("Tööriista saab kustutada ainult selle omanik", exception.getMessage());
        verify(toolDeleteRepository, never()).existsBookingOfTool(9);
        verify(toolDeleteRepository, never()).deleteToolBy(9);
    }

    /**
     * Tööriist, millel on broneeringuid, annab 403 TOOL_HAS_BOOKINGS ja midagi ei kustutata.
     */
    @Test
    void toolWithBookings_producesToolHasBookings() {
        when(toolDeleteRepository.findOwnerIdBy(1)).thenReturn(Optional.of(5));
        when(toolDeleteRepository.existsBookingOfTool(1)).thenReturn(true);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class, () -> toolDeleteService.deleteTool(5, 1));

        assertEquals("TOOL_HAS_BOOKINGS", exception.getErrorCode());
        assertEquals("Tööriista ei saa kustutada, sest sellel on broneeringuid", exception.getMessage());
        verify(toolDeleteRepository, never()).deleteToolImagesOf(1);
        verify(toolDeleteRepository, never()).deleteToolBy(1);
    }

    /**
     * Andmebaasi tõrge muutub 500 InternalServerErrorException'iks kokkulepitud sõnumiga, SQL-i kliendile ei näidata.
     */
    @Test
    void databaseFailure_producesInternalServerErrorWithoutTechnicalDetails() {
        when(toolDeleteRepository.findOwnerIdBy(9)).thenReturn(Optional.of(5));
        when(toolDeleteRepository.deleteToolImagesOf(9)).thenThrow(new DataAccessResourceFailureException("SQL details"));

        InternalServerErrorException exception = assertThrows(
                InternalServerErrorException.class, () -> toolDeleteService.deleteTool(5, 9));

        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCode());
        assertEquals("Tööriista kustutamine ebaõnnestus. Palun proovi hiljem uuesti.", exception.getMessage());
        assertFalse(exception.getMessage().contains("SQL details"));
    }
}
