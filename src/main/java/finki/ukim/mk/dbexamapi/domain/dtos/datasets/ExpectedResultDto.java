package finki.ukim.mk.dbexamapi.domain.dtos.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;

import java.time.Instant;

public record ExpectedResultDto(
        Task task,
        DatasetVersion version,
        TestStep step,
        String hash,
        Instant computedAt
) {
}
