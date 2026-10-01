package ee.toolrental.persistence.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("select (count(b) > 0) from Booking b where b.tool.id = :toolId " +
            "and b.status in ('P', 'C') and b.startDate <= :endDate and b.endDate >= :startDate")
    boolean existsActiveBookingOverlapping(@Param("toolId") Integer toolId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("select b from Booking b join fetch b.tool t " +
            "where b.renter.id = :userId and t.owner.id <> :userId and b.status = 'C' " +
            "and b.startDate <= :today and b.endDate >= :today order by b.startDate asc, b.id asc")
    List<Booking> findCurrentRenterBookingsBy(@Param("userId") Integer userId,
                                              @Param("today") LocalDate today);

    @Query("select b from Booking b join fetch b.tool t " +
            "where t.owner.id = :userId and b.renter.id <> :userId and b.status = 'P' " +
            "and b.endDate >= :today order by b.startDate asc, b.id asc")
    List<Booking> findPendingOwnerBookingsBy(@Param("userId") Integer userId,
                                             @Param("today") LocalDate today);

    @Query("select b from Booking b join fetch b.tool t " +
            "where b.renter.id = :userId and t.owner.id <> :userId and b.status = 'P' " +
            "and b.endDate >= :today order by b.startDate asc, b.id asc")
    List<Booking> findPendingRenterBookingsBy(@Param("userId") Integer userId,
                                              @Param("today") LocalDate today);

    @Query("select b from Booking b join fetch b.tool t " +
            "where t.owner.id = :userId and b.renter.id <> :userId and b.status = 'C' " +
            "and b.startDate <= :today and b.endDate >= :today order by b.startDate asc, b.id asc")
    List<Booking> findCurrentOwnerBookingsBy(@Param("userId") Integer userId,
                                             @Param("today") LocalDate today);
}
