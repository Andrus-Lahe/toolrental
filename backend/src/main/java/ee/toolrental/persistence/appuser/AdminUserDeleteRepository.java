package ee.toolrental.persistence.appuser;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Admini kasutaja kustutamise päringud. Eraldi liides hoiab AppUserRepository, ToolRepository ja
 * BookingRepository muutmata, et teised taskid saaksid neid muuta ilma merge-konfliktita.
 */
public interface AdminUserDeleteRepository extends Repository<AppUser, Integer> {

    // Kontrollib, kas kasutajal on vähemalt üks tööriist (tool.owner_id). Tagastab true või false, mitte nimekirja.
    // Seda kasutab AdminUserDeleteService.deleteUser, et kasutajat, kellel on tööriistu, mitte kustutada.
    @Query("select (count(t) > 0) from Tool t where t.owner.id = :userId")
    boolean existsToolOwnedBy(Integer userId);

    // Kontrollib, kas kasutajal on vähemalt üks broneering üürnikuna (booking.renter_id).
    // Broneeringud säilivad, et teiste kasutajate broneeringute ajalugu ei kaoks.
    @Query("select (count(b) > 0) from Booking b where b.renter.id = :userId")
    boolean existsBookingRentedBy(Integer userId);

    // Kustutab kasutaja profiili rea; kui profiili pole, ei kustutata midagi ja tagastatakse 0.
    // Profiil tuleb kustutada enne kasutajat, sest profile.user_id välisvõtmel puudub ON DELETE CASCADE.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Profile p where p.user.id = :userId")
    int deleteProfileOf(Integer userId);

    // Kustutab app_user rea ja tagastab kustutatud ridade arvu. Profiili location rida jääb alles.
    // Kutsuda alles pärast deleteProfileOf, samas transaktsioonis.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from AppUser a where a.id = :userId")
    int deleteAppUserBy(Integer userId);
}
