package finki.ukim.mk.dbexamapi.domain.dtos.content;

import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;

public record TaskDocumentDto(
        Task task,
        Document document,
        int sortOrder
) {
}
