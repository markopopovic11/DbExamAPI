package finki.ukim.mk.dbexamapi.web.v1.extensions.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TestStepDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.TestStepRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.TestStepResponse;

import java.util.List;

public final class TestStepExtensions {

    private TestStepExtensions() {
    }

    public static TestStepDto toDto(TestStepRequest request, Task task) {
        return new TestStepDto(
                task,
                request.sortOrder(),
                request.actionSql(),
                request.expectError(),
                request.checkSql()
        );
    }

    public static TestStepResponse toResponse(TestStep testStep) {
        return new TestStepResponse(
                testStep.getId(),
                testStep.getTask().getId(),
                testStep.getSortOrder(),
                testStep.getActionSql(),
                testStep.isExpectError(),
                testStep.getCheckSql()
        );
    }

    public static List<TestStepResponse> toResponse(List<TestStep> testSteps) {
        return testSteps.stream()
                .map(TestStepExtensions::toResponse)
                .toList();
    }
}
