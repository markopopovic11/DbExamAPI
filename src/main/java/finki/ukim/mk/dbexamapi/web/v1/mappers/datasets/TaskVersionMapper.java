package finki.ukim.mk.dbexamapi.web.v1.mappers.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetVersionService;
import finki.ukim.mk.dbexamapi.service.datasets.TaskVersionService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.datasets.TaskVersionExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.TaskVersionRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.TaskVersionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskVersionMapper {

    private final TaskVersionService taskVersionService;
    private final TaskService taskService;
    private final DatasetVersionService datasetVersionService;

    public TaskVersionMapper(TaskVersionService taskVersionService,
                             TaskService taskService,
                             DatasetVersionService datasetVersionService) {
        this.taskVersionService = taskVersionService;
        this.taskService = taskService;
        this.datasetVersionService = datasetVersionService;
    }

    public List<TaskVersionResponse> findAll() {
        return TaskVersionExtensions.toResponse(taskVersionService.findAll());
    }

    public TaskVersionResponse findById(String id) {
        return TaskVersionExtensions.toResponse(taskVersionService.findByIdNotNull(id));
    }

    public TaskVersionResponse create(TaskVersionRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        DatasetVersion version = datasetVersionService.findByIdNotNull(request.versionId());
        return TaskVersionExtensions.toResponse(taskVersionService.create(TaskVersionExtensions.toDto(request, task, version)));
    }

    public TaskVersionResponse update(String id, TaskVersionRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        DatasetVersion version = datasetVersionService.findByIdNotNull(request.versionId());
        return TaskVersionExtensions.toResponse(taskVersionService.update(id, TaskVersionExtensions.toDto(request, task, version)));
    }

    public TaskVersionResponse deleteById(String id) {
        return TaskVersionExtensions.toResponse(taskVersionService.deleteById(id));
    }
}
