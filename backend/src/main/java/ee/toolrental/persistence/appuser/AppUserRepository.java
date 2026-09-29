package ee.toolrental.persistence.appuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    // Otsib kasutaja Google'i konto unikaalse ID (google_sub) järgi; päring on kirjutatud @Query sees JPQL-is.
    // Optional on "karp", mis võib olla tühi: kui kasutajat veel pole, on karp tühi ja viga ei visata.
    // Seda kasutab Google'iga sisselogimine (AppUserOidcService), et leida olemasolev või luua uus kasutaja.

    @Query("select a from AppUser a where a.googleSub = :googleSub")
    Optional<AppUser> findAppUserBy(String googleSub);

    // Otsib samuti kasutajaid google_sub järgi, aga siin koostab Spring päringu ise meetodi nimest (...ByGoogleSub).
    // Tagastab nimekirja (List); kuna google_sub on andmebaasis UNIQUE, on seal kõige rohkem üks kasutaja.
    // Kui vastet pole, tagastatakse tühi nimekiri, mitte null.

    List<AppUser> findAppUserByGoogleSub(String googleSub);
}

