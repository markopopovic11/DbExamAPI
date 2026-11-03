package finki.ukim.mk.dbexamapi.web.v1.requests.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExamFolderRequest(
        @NotBlank String examId,
        @NotBlank String folderId,
        @NotNull Integer sortOrder
) {
}
