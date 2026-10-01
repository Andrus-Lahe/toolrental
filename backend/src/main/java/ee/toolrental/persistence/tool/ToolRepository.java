package ee.toolrental.persistence.tool;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tool t where t.id = :toolId")
    Optional<Tool> findToolForBookingBy(@Param("toolId") Integer toolId);
}
