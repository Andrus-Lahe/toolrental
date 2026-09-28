package ee.toolrental.persistence.categoryimage;

import ee.toolrental.persistence.category.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category_image", schema =
        "tool_rental")
@Getter
@Setter


public class CategoryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false, unique = true)

    private Category category;



}
