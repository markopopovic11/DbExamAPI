package finki.ukim.mk.dbexamapi.web.v1.responses.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskObjectKind;

public record TaskObjectResponse(
        String id,
        String taskId,
        TaskObjectKind kind,
        String name,
        String arguments,
        String onTable,
        int sortOrder
) {
}
