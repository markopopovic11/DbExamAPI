package finki.ukim.mk.dbexamapi.web.v1.extensions.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskObjectDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TaskObject;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TaskObjectRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TaskObjectResponse;

import java.util.List;

public final class TaskObjectExtensions {

    private TaskObjectExtensions() {
    }

    public static TaskObjectDto toDto(TaskObjectRequest request, Task task) {
        return new TaskObjectDto(
                task,
                request.kind(),
                request.name(),
                request.arguments(),
                request.onTable(),
                request.sortOrder()
        );
    }

    public static TaskObjectResponse toResponse(TaskObject taskObject) {
        return new TaskObjectResponse(
                taskObject.getId(),
                taskObject.getTask().getId(),
                taskObject.getKind(),
                taskObject.getName(),
                taskObject.getArguments(),
                taskObject.getOnTable(),
                taskObject.getSortOrder()
        );
    }

    public static List<TaskObjectResponse> toResponse(List<TaskObject> taskObjects) {
        return taskObjects.stream()
                .map(TaskObjectExtensions::toResponse)
                .toList();
    }
}
