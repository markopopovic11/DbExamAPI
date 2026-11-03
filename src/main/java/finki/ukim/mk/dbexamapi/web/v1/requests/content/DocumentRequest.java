package finki.ukim.mk.dbexamapi.web.v1.requests.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * For markdown and link documents. Files go through the upload endpoint; on update of a file
 * document, contentMd and url stay null and the file is kept.
 */
public record DocumentRequest(
        String folderId,
        @NotBlank String name,
        String contentMd,
        String url,
        @NotNull Integer sortOrder
) {
}
