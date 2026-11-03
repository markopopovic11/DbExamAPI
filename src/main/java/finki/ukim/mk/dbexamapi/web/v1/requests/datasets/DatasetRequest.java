package finki.ukim.mk.dbexamapi.web.v1.requests.datasets;

import jakarta.validation.constraints.NotBlank;

public record DatasetRequest(
        @NotBlank String examId,
        @NotBlank String name,
        @NotBlank String ddl
) {
}
