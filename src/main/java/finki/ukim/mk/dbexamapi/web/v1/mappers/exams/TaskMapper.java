package finki.ukim.mk.dbexamapi.web.v1.mappers.exams;

import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetService;
import finki.ukim.mk.dbexamapi.service.exams.ExamGroupService;
import finki.ukim.mk.dbexamapi.service.exams.ExamService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.exams.TaskExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TaskRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TaskResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {

    private final TaskService taskService;
    private final ExamService examService;
    private final ExamGroupService examGroupService;
    private final DatasetService datasetService;

    public TaskMapper(TaskService taskService,
                      ExamService examService,
                      ExamGroupService examGroupService,
                      DatasetService datasetService) {
        this.taskService = taskService;
        this.examService = examService;
        this.examGroupService = examGroupService;
        this.datasetService = datasetService;
    }

    public List<TaskResponse> findAll() {
        return TaskExtensions.toResponse(taskService.findAll());
    }

    public TaskResponse findById(String id) {
        return TaskExtensions.toResponse(taskService.findByIdNotNull(id));
    }

    public TaskResponse create(TaskRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        ExamGroup group = request.groupId() == null ? null : examGroupService.findByIdNotNull(request.groupId());
        Task sourceTask = request.sourceTaskId() == null ? null : taskService.findByIdNotNull(request.sourceTaskId());
        Dataset dataset = request.datasetId() == null ? null : datasetService.findByIdNotNull(request.datasetId());
        return TaskExtensions.toResponse(taskService.create(TaskExtensions.toDto(request, exam, group, sourceTask, dataset)));
    }

    public TaskResponse update(String id, TaskRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        ExamGroup group = request.groupId() == null ? null : examGroupService.findByIdNotNull(request.groupId());
        Task sourceTask = request.sourceTaskId() == null ? null : taskService.findByIdNotNull(request.sourceTaskId());
        Dataset dataset = request.datasetId() == null ? null : datasetService.findByIdNotNull(request.datasetId());
        return TaskExtensions.toResponse(taskService.update(id, TaskExtensions.toDto(request, exam, group, sourceTask, dataset)));
    }

    public TaskResponse deleteById(String id) {
        return TaskExtensions.toResponse(taskService.deleteById(id));
    }
}
