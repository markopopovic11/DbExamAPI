package finki.ukim.mk.dbexamapi.domain.dtos.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Task;

public record TestStepDto(
        Task task,
        int sortOrder,
        String actionSql,
        boolean expectError,
        String checkSql
) {
}
