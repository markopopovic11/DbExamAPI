package finki.ukim.mk.dbexamapi.web.v1.extensions.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.TaskVersionDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.datasets.TaskVersion;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.TaskVersionRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.TaskVersionResponse;

import java.util.List;

public final class TaskVersionExtensions {

    private TaskVersionExtensions() {
    }

    public static TaskVersionDto toDto(TaskVersionRequest request, Task task, DatasetVersion version) {
        return new TaskVersionDto(
                task,
                version
        );
    }

    public static TaskVersionResponse toResponse(TaskVersion taskVersion) {
        return new TaskVersionResponse(
                taskVersion.getId(),
                taskVersion.getTask().getId(),
                taskVersion.getVersion().getId()
        );
    }

    public static List<TaskVersionResponse> toResponse(List<TaskVersion> taskVersions) {
        return taskVersions.stream()
                .map(TaskVersionExtensions::toResponse)
                .toList();
    }
}
