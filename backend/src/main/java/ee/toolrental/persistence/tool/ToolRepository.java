package ee.toolrental.persistence.tool;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tool t where t.id = :toolId")
    Optional<Tool> findToolForBookingBy(@Param("toolId") Integer toolId);

    @Query("select t from Tool t where t.owner.id = :userId and t.status = 'A' " +
            "and not exists (select b.id from Booking b where b.tool = t and b.status = 'C' " +
            "and b.startDate <= :today and b.endDate >= :today) " +
            "order by t.createdAt desc, t.id desc")
    List<Tool> findAvailableOwnerToolsBy(@Param("userId") Integer userId,
                                         @Param("today") LocalDate today);

    /** Tool list filters are combined with AND; zero disables a filter. */
    @Query(value = """
            select new ee.toolrental.persistence.tool.ToolListRow(
                t.id, t.name, t.description, ti.imageData, t.status, c.cityName, d.districtName)
            from Tool t
            left join Profile p on p.user = t.owner
            left join p.location l
            left join l.district d
            left join d.city c
            left join ToolImage ti on ti.tool = t and ti.main = true
            where (:categoryId = 0 or t.category.id = :categoryId)
              and (:cityId = 0 or c.id = :cityId)
              and (:districtId = 0 or d.id = :districtId)
              and (:status = '0' or t.status = :status)
            order by t.id asc
            """, countQuery = """
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
    Page<ToolListRow> findToolListRowsBy(@Param("categoryId") Integer categoryId,
            @Param("cityId") Integer cityId, @Param("districtId") Integer districtId,
            @Param("status") String status, Pageable pageable);

}
