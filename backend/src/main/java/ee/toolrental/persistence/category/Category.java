package ee.toolrental.persistence.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category", schema = "tool_rental")
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Integer id;
    @Column(name = "category_name", nullable = false, length = 100)

    private String categoryName;
    @Column(name = "description")


    private String description;
    @Column(name = "sequence", nullable = false)
    private Integer sequence;


}
