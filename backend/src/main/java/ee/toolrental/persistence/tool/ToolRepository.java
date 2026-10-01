package ee.toolrental.persistence.tool;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
