package finki.ukim.mk.dbexamapi.web.v1.extensions.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.TaskDocumentDto;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.content.TaskDocument;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.TaskDocumentRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.TaskDocumentResponse;

import java.util.List;

public final class TaskDocumentExtensions {

    private TaskDocumentExtensions() {
    }

    public static TaskDocumentDto toDto(TaskDocumentRequest request, Task task, Document document) {
        return new TaskDocumentDto(
                task,
                document,
                request.sortOrder()
        );
    }

    public static TaskDocumentResponse toResponse(TaskDocument taskDocument) {
        return new TaskDocumentResponse(
                taskDocument.getId(),
                taskDocument.getTask().getId(),
                taskDocument.getDocument().getId(),
                taskDocument.getSortOrder()
        );
    }

    public static List<TaskDocumentResponse> toResponse(List<TaskDocument> taskDocuments) {
        return taskDocuments.stream()
                .map(TaskDocumentExtensions::toResponse)
                .toList();
    }
}
