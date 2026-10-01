package ee.toolrental.service;

import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AdminUserDeleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminUserDeleteService {

    private static final String USER_DELETING_FAILED = "Kasutaja kustutamine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String SELF_DELETE_NOT_ALLOWED_MESSAGE = "Iseennast ei saa kustutada";
    private static final String SELF_DELETE_NOT_ALLOWED = "SELF_DELETE_NOT_ALLOWED";
    private static final String USER_HAS_DATA_MESSAGE = "Kasutajat ei saa kustutada, sest tal on tööriistu või broneeringuid";
    private static final String USER_HAS_DATA = "USER_HAS_DATA";

    private final AdminUserDeleteRepository adminUserDeleteRepository;
    private final AppUserService appUserService;

    /**
     * Kustutab kasutaja ühes transaktsioonis: kõigepealt profiili (kui on), seejärel app_user rea.
     * Kontrollide järjekord: kasutaja olemasolu (404), iseenda kustutamine (403), tööriistad või broneeringud (403).
     * Andmebaasi tõrge muudetakse 500 vastuseks ja kõik muudatused võetakse tagasi.
     */
    @Transactional
    public void deleteUser(Integer actorUserId, Integer userId) {
        try {
            appUserService.getValidAppUserBy(userId);
            validateIsNotSelf(actorUserId, userId);
            validateHasNoData(userId);
            adminUserDeleteRepository.deleteProfileOf(userId);
            adminUserDeleteRepository.deleteAppUserBy(userId);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(USER_DELETING_FAILED);
        }
    }

    /**
     * Keelab admini enda kustutamise: kui sessiooni kasutaja ID ja kustutatava ID on samad, visatakse 403.
     * Sessiooni kasutaja ID tuleb alati serverist, klient seda asendada ei saa.
     */
    private void validateIsNotSelf(Integer actorUserId, Integer userId) {
        if (actorUserId.equals(userId)) {
            throw new ForbiddenException(SELF_DELETE_NOT_ALLOWED_MESSAGE, SELF_DELETE_NOT_ALLOWED);
        }
    }

    /**
     * Keelab kustutamise, kui kasutajal on vähemalt üks tööriist või broneering (403 USER_HAS_DATA).
     * Nii jäävad tool ja booking välisvõtmed terveks ning teiste kasutajate ajalugu ei kao.
     */
    private void validateHasNoData(Integer userId) {
        if (adminUserDeleteRepository.existsToolOwnedBy(userId) || adminUserDeleteRepository.existsBookingRentedBy(userId)) {
            throw new ForbiddenException(USER_HAS_DATA_MESSAGE, USER_HAS_DATA);
        }
    }
}
