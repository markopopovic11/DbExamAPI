package finki.ukim.mk.dbexamapi.service.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.TaskDocumentDto;
import finki.ukim.mk.dbexamapi.domain.models.content.TaskDocument;

import java.util.List;
import java.util.Optional;

public interface TaskDocumentService {

    Optional<TaskDocument> findById(String id);

    TaskDocument findByIdNotNull(String id);

    List<TaskDocument> findAll();

    TaskDocument create(TaskDocumentDto taskDocumentDto);

    TaskDocument update(String id, TaskDocumentDto taskDocumentDto);

    TaskDocument deleteById(String id);

    boolean existsByDocumentId(String documentId);

    void deleteAllByTaskId(String taskId);
}
