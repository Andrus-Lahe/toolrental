package ee.toolrental.service;

import ee.toolrental.controller.admin.dto.AdminUserDto;
import ee.toolrental.infrastructure.exception.InternalServerErrorException;
import ee.toolrental.persistence.appuser.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminUserListService {

    private static final String USERS_LOADING_FAILED = "Kasutajate laadimine ebaõnnestus. Palun proovi hiljem uuesti.";

    private final AdminUserRepository adminUserRepository;

    /**
     * Tagastab kõik süsteemi kasutajad admini tabeli jaoks järjestuses app_user.id ASC.
     * Andmebaasi tõrge muudetakse 500 vastuseks (InternalServerErrorException), SQL-i kliendile ei näidata.
     */
    @Transactional(readOnly = true)
    public List<AdminUserDto> getUsers() {
        try {
            return adminUserRepository.findAllUsersWithProfilesBy();
        } catch (DataAccessException e) {
            throw new InternalServerErrorException(USERS_LOADING_FAILED);
        }
    }
}
