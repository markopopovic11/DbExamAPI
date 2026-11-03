package finki.ukim.mk.dbexamapi.web.v1.requests.exams;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Boolean active
) {
}
