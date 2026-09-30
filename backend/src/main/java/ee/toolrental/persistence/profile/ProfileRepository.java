package ee.toolrental.persistence.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {

    @Query("select a from Profile a where a.user.id = :userId")
    Optional<Profile> findProfileBy(Integer userId);

    @Query("select (count(p) > 0) from Profile p where p.email = :email and p.user.id <> :userId")
    boolean existsOtherUserProfileBy(String email, Integer userId);
}
