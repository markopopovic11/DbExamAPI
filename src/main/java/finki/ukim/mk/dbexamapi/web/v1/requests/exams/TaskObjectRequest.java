package finki.ukim.mk.dbexamapi.web.v1.requests.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskObjectKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskObjectRequest(
        @NotBlank String taskId,
        @NotNull TaskObjectKind kind,
        @NotBlank String name,
        String arguments,
        String onTable,
        @NotNull Integer sortOrder
) {
}
