package ee.toolrental.persistence.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("select (count(b) > 0) from Booking b where b.tool.id = :toolId " +
            "and b.status in ('P', 'C') and b.startDate <= :endDate and b.endDate >= :startDate")
    boolean existsActiveBookingOverlapping(@Param("toolId") Integer toolId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
