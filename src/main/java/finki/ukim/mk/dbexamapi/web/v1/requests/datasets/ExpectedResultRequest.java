package finki.ukim.mk.dbexamapi.web.v1.requests.datasets;

import jakarta.validation.constraints.NotBlank;

public record ExpectedResultRequest(
        @NotBlank String taskId,
        @NotBlank String versionId,
        String stepId,
        @NotBlank String hash
) {
}
