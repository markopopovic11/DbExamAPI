package finki.ukim.mk.dbexamapi.repository.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.TaskVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskVersionRepository extends JpaRepository<TaskVersion, String> {

    boolean existsByTask_IdAndVersion_Id(String taskId, String versionId);

    boolean existsByTask_IdAndVersion_IdAndIdNot(String taskId, String versionId, String id);

    boolean existsByVersion_Id(String versionId);

    void deleteAllByTask_Id(String taskId);
}
