package finki.ukim.mk.dbexamapi.domain.dtos.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskObjectKind;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;

public record TaskObjectDto(
        Task task,
        TaskObjectKind kind,
        String name,
        String arguments,
        String onTable,
        int sortOrder
) {
}
