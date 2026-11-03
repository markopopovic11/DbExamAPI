package finki.ukim.mk.dbexamapi.repository.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestStepRepository extends JpaRepository<TestStep, String> {

    boolean existsByTask_IdAndSortOrder(String taskId, int sortOrder);

    boolean existsByTask_IdAndSortOrderAndIdNot(String taskId, int sortOrder, String id);

    void deleteAllByTask_Id(String taskId);
}
