package finki.ukim.mk.dbexamapi.web.v1.requests.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskDocumentRequest(
        @NotBlank String taskId,
        @NotBlank String documentId,
        @NotNull Integer sortOrder
) {
}
