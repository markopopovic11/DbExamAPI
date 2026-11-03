package finki.ukim.mk.dbexamapi.web.v1.mappers.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.service.exams.TaskObjectService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.exams.TaskObjectExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TaskObjectRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TaskObjectResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskObjectMapper {

    private final TaskObjectService taskObjectService;
    private final TaskService taskService;

    public TaskObjectMapper(TaskObjectService taskObjectService,
                            TaskService taskService) {
        this.taskObjectService = taskObjectService;
        this.taskService = taskService;
    }

    public List<TaskObjectResponse> findAll() {
        return TaskObjectExtensions.toResponse(taskObjectService.findAll());
    }

    public TaskObjectResponse findById(String id) {
        return TaskObjectExtensions.toResponse(taskObjectService.findByIdNotNull(id));
    }

    public TaskObjectResponse create(TaskObjectRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        return TaskObjectExtensions.toResponse(taskObjectService.create(TaskObjectExtensions.toDto(request, task)));
    }

    public TaskObjectResponse update(String id, TaskObjectRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        return TaskObjectExtensions.toResponse(taskObjectService.update(id, TaskObjectExtensions.toDto(request, task)));
    }

    public TaskObjectResponse deleteById(String id) {
        return TaskObjectExtensions.toResponse(taskObjectService.deleteById(id));
    }
}
