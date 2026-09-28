package ee.toolrental.persistence.profile;

import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {

    //TODO: lisa email'i kontroll

    @Query("select a email from profile a where a.googleSub = :googleSub")
    Optional<Email> findemail(String googleSub);

}