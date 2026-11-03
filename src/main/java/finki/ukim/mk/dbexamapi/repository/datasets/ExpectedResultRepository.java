package finki.ukim.mk.dbexamapi.repository.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.ExpectedResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpectedResultRepository extends JpaRepository<ExpectedResult, String> {

    boolean existsByTask_IdAndVersion_IdAndStepIsNull(String taskId, String versionId);

    boolean existsByTask_IdAndVersion_IdAndStepIsNullAndIdNot(String taskId, String versionId, String id);

    boolean existsByTask_IdAndVersion_IdAndStep_Id(String taskId, String versionId, String stepId);

    boolean existsByTask_IdAndVersion_IdAndStep_IdAndIdNot(String taskId, String versionId, String stepId, String id);

    void deleteAllByTask_Id(String taskId);

    void deleteAllByTask_IdAndVersion_Id(String taskId, String versionId);

    void deleteAllByStep_Id(String stepId);

    void deleteAllByStep_Task_Id(String taskId);
}
