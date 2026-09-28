package ee.toolrental.persistence.category;

import jakarta.persistence.*;

@Entity
@Table(name = "category", schema = "tool_rental")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Integer id;
    @Column(name = "category_name", nullable = false, length = 100)

    private String categoryName;
    @Column(name = "description")

    private String description;


}
