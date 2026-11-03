package finki.ukim.mk.dbexamapi.repository.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.TaskObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskObjectRepository extends JpaRepository<TaskObject, String> {

    /**
     * True when an object with this name (case-insensitive, like unquoted Postgres names) belongs
     * to a task of the exam that is visible to the same group. Same group rule as task sort order.
     * excludeId null on create.
     */
    @Query("""
            select count(o) > 0 from TaskObject o join o.task t left join t.group g
            where t.exam.id = :examId
              and lower(o.name) = lower(:name)
              and (:excludeId is null or o.id <> :excludeId)
              and (:groupId is null or g is null or g.id = :groupId)
            """)
    boolean existsNameInGroupView(@Param("examId") String examId,
                                  @Param("groupId") String groupId,
                                  @Param("name") String name,
                                  @Param("excludeId") String excludeId);

    boolean existsByTask_Id(String taskId);

    boolean existsByTask_IdAndIdNot(String taskId, String id);

    void deleteAllByTask_Id(String taskId);
}
