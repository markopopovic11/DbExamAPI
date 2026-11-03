package finki.ukim.mk.dbexamapi.web.v1.responses.content;

import java.time.Instant;

public record DocumentResponse(
        String id,
        String folderId,
        String name,
        String contentMd,
        String url,
        boolean hasFile,
        String mimeType,
        Integer sizeBytes,
        int sortOrder,
        Instant updatedAt
) {
}
