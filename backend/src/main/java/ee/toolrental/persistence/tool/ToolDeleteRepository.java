package ee.toolrental.persistence.tool;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Tööriista kustutamise päringud. Eraldi liides hoiab ToolRepository, ToolImageRepository ja
 * BookingRepository muutmata, et teised taskid saaksid neid muuta ilma merge-konfliktita.
 */
public interface ToolDeleteRepository extends Repository<Tool, Integer> {

    // Leiab tööriista omaniku (app_user.id) tööriista ID järgi; kui tööriista pole, on tulemus tühi.
    // Tagastab ainult omaniku ID, mitte kogu tööriista (pilte ei laeta). Seda kasutab ToolDeleteService.deleteTool.
    @Query("select t.owner.id from Tool t where t.id = :toolId")
    Optional<Integer> findOwnerIdBy(Integer toolId);

    // Kontrollib, kas tööriistal on vähemalt üks broneering (booking.tool_id), olenemata broneeringu staatusest.
    // Tagastab true või false, mitte nimekirja. Broneeringud säilivad, et teiste kasutajate ajalugu ei kaoks.
    @Query("select (count(b) > 0) from Booking b where b.tool.id = :toolId")
    boolean existsBookingOfTool(Integer toolId);

    // Kustutab tööriista pildid (tool_image); kui pilte pole, ei kustutata midagi ja tagastatakse 0.
    // Kustutatakse enne tööriista, et välisvõti ei takistaks (skeemis on küll ON DELETE CASCADE, aga see on selgem).
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ToolImage ti where ti.tool.id = :toolId")
    int deleteToolImagesOf(Integer toolId);

    // Kustutab tool rea ja tagastab kustutatud ridade arvu.
    // Kutsuda alles pärast deleteToolImagesOf, samas transaktsioonis.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Tool t where t.id = :toolId")
    int deleteToolBy(Integer toolId);
}
