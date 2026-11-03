package finki.ukim.mk.dbexamapi.domain.dtos.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;

public record TaskVersionDto(
        Task task,
        DatasetVersion version
) {
}
