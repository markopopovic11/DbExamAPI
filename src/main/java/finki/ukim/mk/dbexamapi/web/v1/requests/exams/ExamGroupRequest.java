package finki.ukim.mk.dbexamapi.web.v1.requests.exams;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExamGroupRequest(
        @NotBlank String examId,
        @NotBlank String name,
        @NotNull Integer sortOrder
) {
}
