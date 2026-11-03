package finki.ukim.mk.dbexamapi.web.v1.requests.exams;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ExamRequest(
        @NotBlank String courseId,
        @NotBlank String name,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt
) {
}
