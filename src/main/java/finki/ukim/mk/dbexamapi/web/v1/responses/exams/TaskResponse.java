package finki.ukim.mk.dbexamapi.web.v1.responses.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskDataSource;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;

public record TaskResponse(
        String id,
        String examId,
        String groupId,
        TaskType type,
        TaskDataSource dataSource,
        String sourceTaskId,
        String datasetId,
        String solution,
        String title,
        String description,
        int points,
        int sortOrder
) {
}
