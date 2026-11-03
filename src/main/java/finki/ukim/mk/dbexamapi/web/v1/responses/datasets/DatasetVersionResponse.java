package finki.ukim.mk.dbexamapi.web.v1.responses.datasets;

import java.time.Instant;

public record DatasetVersionResponse(
        String id,
        String datasetId,
        String name,
        boolean visible,
        String documentId,
        int sortOrder,
        Instant updatedAt
) {
}
