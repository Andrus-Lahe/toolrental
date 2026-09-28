package ee.toolrental.persistence.appuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    @Query("select a from AppUser a where a.googleSub = :googleSub")
    Optional<AppUser> findAppUserBy(String googleSub);
}

