package finki.ukim.mk.dbexamapi.domain.dtos.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;

public record ExamGroupDto(
        Exam exam,
        String name,
        int sortOrder
) {
}
