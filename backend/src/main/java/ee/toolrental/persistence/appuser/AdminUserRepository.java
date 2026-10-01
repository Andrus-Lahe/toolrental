package ee.toolrental.persistence.appuser;

import ee.toolrental.controller.admin.dto.AdminUserDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/**
 * Admini kasutajate nimekirja päring. Eraldi liides hoiab AppUserRepository muutmata,
 * et teised admini taskid (blokeerimine, kustutamine) saaksid seda muuta ilma merge-konfliktita.
 */
public interface AdminUserRepository extends Repository<AppUser, Integer> {

    // Leiab kõik kasutajad admini tabeli jaoks, id järgi kasvavalt; päring projekteerib tulemuse otse AdminUserDto-sse.
    // LEFT JOIN hoiab profiilita kasutaja loendis (email ja registeredAt on siis null); google_sub-i ei võeta.
    // registeredAt on profile.created_at kuupäevaosa, mida kasutab AdminUserListService.getUsers.
    @Query("""
            select new ee.toolrental.controller.admin.dto.AdminUserDto(
                a.id, a.firstName, a.lastName, p.email, r.roleName, cast(p.createdAt as LocalDate), a.status)
            from AppUser a
            join a.role r
            left join Profile p on p.user = a
            order by a.id asc""")
    List<AdminUserDto> findAllUsersWithProfilesBy();
}
