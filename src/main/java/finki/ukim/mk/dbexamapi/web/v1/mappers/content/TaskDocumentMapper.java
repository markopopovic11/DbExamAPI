package finki.ukim.mk.dbexamapi.web.v1.mappers.content;

import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.service.content.DocumentService;
import finki.ukim.mk.dbexamapi.service.content.TaskDocumentService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.content.TaskDocumentExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.TaskDocumentRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.TaskDocumentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskDocumentMapper {

    private final TaskDocumentService taskDocumentService;
    private final TaskService taskService;
    private final DocumentService documentService;

    public TaskDocumentMapper(TaskDocumentService taskDocumentService,
                              TaskService taskService,
                              DocumentService documentService) {
        this.taskDocumentService = taskDocumentService;
        this.taskService = taskService;
        this.documentService = documentService;
    }

    public List<TaskDocumentResponse> findAll() {
        return TaskDocumentExtensions.toResponse(taskDocumentService.findAll());
    }

    public TaskDocumentResponse findById(String id) {
        return TaskDocumentExtensions.toResponse(taskDocumentService.findByIdNotNull(id));
    }

    public TaskDocumentResponse create(TaskDocumentRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        Document document = documentService.findByIdNotNull(request.documentId());
        return TaskDocumentExtensions.toResponse(taskDocumentService.create(TaskDocumentExtensions.toDto(request, task, document)));
    }

    public TaskDocumentResponse update(String id, TaskDocumentRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        Document document = documentService.findByIdNotNull(request.documentId());
        return TaskDocumentExtensions.toResponse(taskDocumentService.update(id, TaskDocumentExtensions.toDto(request, task, document)));
    }

    public TaskDocumentResponse deleteById(String id) {
        return TaskDocumentExtensions.toResponse(taskDocumentService.deleteById(id));
    }
}
