package finki.ukim.mk.dbexamapi.repository.content;

import finki.ukim.mk.dbexamapi.domain.models.content.TaskDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDocumentRepository extends JpaRepository<TaskDocument, String> {

    boolean existsByTask_IdAndDocument_Id(String taskId, String documentId);

    boolean existsByTask_IdAndDocument_IdAndIdNot(String taskId, String documentId, String id);

    boolean existsByDocument_Id(String documentId);

    void deleteAllByTask_Id(String taskId);
}
