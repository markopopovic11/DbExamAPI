package finki.ukim.mk.dbexamapi.web.v1.mappers.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;
import finki.ukim.mk.dbexamapi.service.datasets.DatasetVersionService;
import finki.ukim.mk.dbexamapi.service.datasets.ExpectedResultService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.service.exams.TestStepService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.datasets.ExpectedResultExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.ExpectedResultRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.ExpectedResultResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpectedResultMapper {

    private final ExpectedResultService expectedResultService;
    private final TaskService taskService;
    private final DatasetVersionService datasetVersionService;
    private final TestStepService testStepService;

    public ExpectedResultMapper(ExpectedResultService expectedResultService,
                                TaskService taskService,
                                DatasetVersionService datasetVersionService,
                                TestStepService testStepService) {
        this.expectedResultService = expectedResultService;
        this.taskService = taskService;
        this.datasetVersionService = datasetVersionService;
        this.testStepService = testStepService;
    }

    public List<ExpectedResultResponse> findAll() {
        return ExpectedResultExtensions.toResponse(expectedResultService.findAll());
    }

    public ExpectedResultResponse findById(String id) {
        return ExpectedResultExtensions.toResponse(expectedResultService.findByIdNotNull(id));
    }

    public ExpectedResultResponse create(ExpectedResultRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        DatasetVersion version = datasetVersionService.findByIdNotNull(request.versionId());
        TestStep step = request.stepId() == null ? null : testStepService.findByIdNotNull(request.stepId());
        return ExpectedResultExtensions.toResponse(expectedResultService.create(ExpectedResultExtensions.toDto(request, task, version, step)));
    }

    public ExpectedResultResponse update(String id, ExpectedResultRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        DatasetVersion version = datasetVersionService.findByIdNotNull(request.versionId());
        TestStep step = request.stepId() == null ? null : testStepService.findByIdNotNull(request.stepId());
        return ExpectedResultExtensions.toResponse(expectedResultService.update(id, ExpectedResultExtensions.toDto(request, task, version, step)));
    }

    public ExpectedResultResponse deleteById(String id) {
        return ExpectedResultExtensions.toResponse(expectedResultService.deleteById(id));
    }
}
