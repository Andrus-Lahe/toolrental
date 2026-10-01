package ee.toolrental.persistence.toolimage;

import ee.toolrental.persistence.tool.Tool;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tool_image", schema = "tool_rental")
public class ToolImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "image_data", nullable = false, columnDefinition = "bytea")
    private byte[] imageData;

    @Column(name = "is_main", nullable = false)
    private boolean main;
    public Boolean getIsMain() {
        return main;
    }

    public void setIsMain(Boolean isMain) {
        this.main = Boolean.TRUE.equals(isMain);
    }
}
