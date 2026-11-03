package finki.ukim.mk.dbexamapi.repository.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    /**
     * True when another task of the exam with this sort order is visible to the same group.
     * A shared task (groupId null) clashes with every task of the exam; a group task clashes
     * with the shared tasks and its own group's tasks. excludeId null on create.
     */
    @Query("""
            select count(t) > 0 from Task t left join t.group g
            where t.exam.id = :examId
              and t.sortOrder = :sortOrder
              and (:excludeId is null or t.id <> :excludeId)
              and (:groupId is null or g is null or g.id = :groupId)
            """)
    boolean existsSortOrderInGroupView(@Param("examId") String examId,
                                       @Param("groupId") String groupId,
                                       @Param("sortOrder") int sortOrder,
                                       @Param("excludeId") String excludeId);

    boolean existsByGroup_Id(String groupId);

    boolean existsBySourceTask_Id(String sourceTaskId);

    boolean existsByDataset_Id(String datasetId);
}
