package finki.ukim.mk.dbexamapi.web.v1.requests.datasets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DatasetVersionRequest(
        @NotBlank String datasetId,
        @NotBlank String name,
        @NotNull Boolean visible,
        @NotBlank String documentId,
        @NotNull Integer sortOrder
) {
}
