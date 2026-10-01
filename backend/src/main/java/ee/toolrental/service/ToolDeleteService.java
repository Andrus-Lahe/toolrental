package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.toolrental.persistence.tool.ToolDeleteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ToolDeleteService {

    private static final String TOOL_DELETING_FAILED = "Tööriista kustutamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String TOOL_NOT_OWNED_MESSAGE = "Tööriista saab kustutada ainult selle omanik";
    private static final String TOOL_NOT_OWNED = "TOOL_NOT_OWNED";
    private static final String TOOL_HAS_BOOKINGS_MESSAGE = "Tööriista ei saa kustutada, sest sellel on broneeringuid";
    private static final String TOOL_HAS_BOOKINGS = "TOOL_HAS_BOOKINGS";

    private final ToolDeleteRepository toolDeleteRepository;

    /**
     * Kustutab sisselogitud kasutaja tööriista ühes transaktsioonis: kõigepealt pildid, seejärel tööriista rea.
     * Kontrollide järjekord: tööriista olemasolu (404), omanik (403 TOOL_NOT_OWNED), broneeringud (403 TOOL_HAS_BOOKINGS).
     * Andmebaasi tõrge logitakse, muudetakse 500 vastuseks ja kõik muudatused võetakse tagasi.
     */
    @Transactional
    public void deleteTool(Integer actorUserId, Integer toolId) {
        try {
            Integer ownerId = getValidOwnerIdBy(toolId);
            validateIsOwner(actorUserId, ownerId);
            validateHasNoBookings(toolId);
            toolDeleteRepository.deleteToolImagesOf(toolId);
            toolDeleteRepository.deleteToolBy(toolId);
        } catch (DataAccessException e) {
            log.error("Tööriista kustutamine ebaõnnestus (userId={}, toolId={})", actorUserId, toolId, e);
            throw new InternalServerErrorException(TOOL_DELETING_FAILED);
        }
    }

    /**
     * Leiab tööriista omaniku ID; kui sellist tööriista pole, visatakse PrimaryKeyNotFoundException (404).
     * Nimi getValid... lubab, et tagastatakse alati päris omanik.
     */
    private Integer getValidOwnerIdBy(Integer toolId) {
        return toolDeleteRepository.findOwnerIdBy(toolId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("toolId", toolId));
    }

    /**
     * Lubab kustutada ainult tööriista omanikul: kui sessiooni kasutaja ID ja omaniku ID erinevad, visatakse 403.
     * Sessiooni kasutaja ID tuleb alati serverist, klient seda asendada ei saa.
     */
    private void validateIsOwner(Integer actorUserId, Integer ownerId) {
        if (!actorUserId.equals(ownerId)) {
            throw new ForbiddenException(TOOL_NOT_OWNED_MESSAGE, TOOL_NOT_OWNED);
        }
    }

    /**
     * Keelab kustutamise, kui tööriistal on vähemalt üks broneering (403 TOOL_HAS_BOOKINGS).
     * Nii jäävad booking.tool_id välisvõtmed terveks ja broneeringute ajalugu ei kao.
     */
    private void validateHasNoBookings(Integer toolId) {
        if (toolDeleteRepository.existsBookingOfTool(toolId)) {
            throw new ForbiddenException(TOOL_HAS_BOOKINGS_MESSAGE, TOOL_HAS_BOOKINGS);
        }
    }
}
