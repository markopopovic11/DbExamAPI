package finki.ukim.mk.dbexamapi.web.v1.responses.datasets;

import java.time.Instant;

public record ExpectedResultResponse(
        String id,
        String taskId,
        String versionId,
        String stepId,
        String hash,
        Instant computedAt
) {
}
