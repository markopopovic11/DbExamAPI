package finki.ukim.mk.dbexamapi.web.v1.requests.exams;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestStepRequest(
        @NotBlank String taskId,
        @NotNull Integer sortOrder,
        @NotBlank String actionSql,
        @NotNull Boolean expectError,
        String checkSql
) {
}
