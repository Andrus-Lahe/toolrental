package ee.toolrental.persistence.location;

import ee.toolrental.persistence.district.District;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "location", schema = "tool_rental")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Size(max = 150)
    @NotNull
    @Column(name = "street_name", nullable = false, length = 150)
    private String streetName;

    @Size(max = 20)
    @NotNull
    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Size(max = 20)
    @Column(name = "apartment_number", length = 20)
    private String apartmentNumber;

    @Column(name = "lng", precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(name = "lat", precision = 10, scale = 7)
    private BigDecimal lat;


}