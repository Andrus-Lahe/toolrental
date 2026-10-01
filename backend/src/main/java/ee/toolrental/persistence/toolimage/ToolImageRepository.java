package ee.toolrental.persistence.toolimage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ToolImageRepository extends JpaRepository<ToolImage, Integer> {
    @Query("select i from ToolImage i join fetch i.tool t where i.main = true and t.id in :toolIds")
    List<ToolImage> findMainToolImagesByToolIds(@Param("toolIds") Collection<Integer> toolIds);

    @Query("select i from ToolImage i where i.tool.id = :toolId and i.main = true order by i.id desc limit 1")
    Optional<ToolImage> findMainToolImageBy(@Param("toolId") Integer toolId);
}
