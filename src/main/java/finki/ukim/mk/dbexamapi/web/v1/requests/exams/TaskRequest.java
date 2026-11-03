package finki.ukim.mk.dbexamapi.web.v1.requests.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskDataSource;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TaskRequest(
        @NotBlank String examId,
        String groupId,
        @NotNull TaskType type,
        TaskDataSource dataSource,
        String sourceTaskId,
        String datasetId,
        String solution,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @PositiveOrZero Integer points,
        @NotNull Integer sortOrder
) {
}
