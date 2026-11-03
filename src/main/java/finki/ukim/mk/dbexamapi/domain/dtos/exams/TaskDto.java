package finki.ukim.mk.dbexamapi.domain.dtos.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskDataSource;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;

public record TaskDto(
        Exam exam,
        ExamGroup group,
        TaskType type,
        TaskDataSource dataSource,
        Task sourceTask,
        Dataset dataset,
        String solution,
        String title,
        String description,
        int points,
        int sortOrder
) {
}
