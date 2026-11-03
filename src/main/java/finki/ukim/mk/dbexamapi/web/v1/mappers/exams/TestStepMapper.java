package finki.ukim.mk.dbexamapi.web.v1.mappers.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.service.exams.TestStepService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.exams.TestStepExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TestStepRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TestStepResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestStepMapper {

    private final TestStepService testStepService;
    private final TaskService taskService;

    public TestStepMapper(TestStepService testStepService,
                          TaskService taskService) {
        this.testStepService = testStepService;
        this.taskService = taskService;
    }

    public List<TestStepResponse> findAll() {
        return TestStepExtensions.toResponse(testStepService.findAll());
    }

    public TestStepResponse findById(String id) {
        return TestStepExtensions.toResponse(testStepService.findByIdNotNull(id));
    }

    public TestStepResponse create(TestStepRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        return TestStepExtensions.toResponse(testStepService.create(TestStepExtensions.toDto(request, task)));
    }

    public TestStepResponse update(String id, TestStepRequest request) {
        Task task = taskService.findByIdNotNull(request.taskId());
        return TestStepExtensions.toResponse(testStepService.update(id, TestStepExtensions.toDto(request, task)));
    }

    public TestStepResponse deleteById(String id) {
        return TestStepExtensions.toResponse(testStepService.deleteById(id));
    }
}
