package ee.toolrental.persistence.tool;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ToolRepository extends JpaRepository<Tool, Integer> {

    /**
     * ID-filtri väärtus 0 ja staatuse väärtus '0' tähendavad, et vastavat filtrit ei rakendata.
     * Kõik seosed on LEFT JOIN-iga, et pildita tööriist ja profiilita omaniku tööriist säiliksid.
     */
    @Query(value = """
            select new ee.toolrental.persistence.tool.ToolListRow(
                t.id, t.name, t.description, ti.imageData, t.status, c.cityName, d.districtName)
            from Tool t
            left join Profile p on p.user = t.owner
            left join p.location l
            left join l.district d
            left join d.city c
            left join ToolImage ti on ti.tool = t and ti.isMain = true
            where (:categoryId = 0 or t.category.id = :categoryId)
              and (:cityId = 0 or c.id = :cityId)
              and (:districtId = 0 or d.id = :districtId)
              and (:status = '0' or t.status = :status)
            order by t.id asc
            """,
            countQuery = """
            select count(t)
            from Tool t
            left join Profile p on p.user = t.owner
            left join p.location l
            left join l.district d
            left join d.city c
            where (:categoryId = 0 or t.category.id = :categoryId)
              and (:cityId = 0 or c.id = :cityId)
              and (:districtId = 0 or d.id = :districtId)
              and (:status = '0' or t.status = :status)
            """)
    Page<ToolListRow> findToolListRowsBy(Integer categoryId, Integer cityId, Integer districtId, String status, Pageable pageable);

}
