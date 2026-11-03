package finki.ukim.mk.dbexamapi.domain.dtos.datasets;

import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;

public record DatasetDto(
        Exam exam,
        String name,
        String ddl
) {
}
