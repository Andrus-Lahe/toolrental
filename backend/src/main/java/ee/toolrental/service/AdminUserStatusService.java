package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.UserStatusRequestDto;
import ee.toolrental.infrastructure.exception.ForbiddenException;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AppUser;
import ee.toolrental.persistence.appuser.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminUserStatusService {

    private static final String STATUS_BLOCKED = "B";
    private static final String USER_STATUS_CHANGING_FAILED = "Kasutaja oleku muutmine ebaõnnestus. Palun proovi hiljem uuesti.";
    private static final String SELF_BLOCK_NOT_ALLOWED_MESSAGE = "Iseennast ei saa blokeerida";
    private static final String SELF_BLOCK_NOT_ALLOWED = "SELF_BLOCK_NOT_ALLOWED";

    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;

    /**
     * Muudab kasutaja oleku (app_user.status) väärtuseks A või B ja salvestab selle kohe andmebaasi.
     * Kontrollide järjekord: kasutaja olemasolu (404), siis iseenda blokeerimine (403); sama olek on samuti 200.
     * Andmebaasi tõrge muudetakse 500 vastuseks ja muudatus võetakse tagasi.
     */
    @Transactional
    public void changeUserStatus(Integer actorUserId, Integer userId, UserStatusRequestDto userStatusRequestDto) {
        try {
            AppUser appUser = appUserService.getValidAppUserBy(userId);
            validateIsNotSelfBlock(actorUserId, userId, userStatusRequestDto.getStatus());
            appUser.setStatus(userStatusRequestDto.getStatus());
            appUserRepository.saveAndFlush(appUser);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(USER_STATUS_CHANGING_FAILED);
        }
    }

    /**
     * Keelab admini enda blokeerimise: 403 visatakse ainult siis, kui ID-d on samad ja uus olek on B.
     * Enda aktiveerimine (A) on lubatud, sest see ei lukusta kontot välja.
     */
    private void validateIsNotSelfBlock(Integer actorUserId, Integer userId, String status) {
        if (actorUserId.equals(userId) && STATUS_BLOCKED.equals(status)) {
            throw new ForbiddenException(SELF_BLOCK_NOT_ALLOWED_MESSAGE, SELF_BLOCK_NOT_ALLOWED);
        }
    }
}
