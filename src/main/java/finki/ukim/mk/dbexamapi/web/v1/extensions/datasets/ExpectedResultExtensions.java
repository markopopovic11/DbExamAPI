package finki.ukim.mk.dbexamapi.web.v1.extensions.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.ExpectedResultDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import finki.ukim.mk.dbexamapi.domain.models.datasets.ExpectedResult;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;
import finki.ukim.mk.dbexamapi.web.v1.requests.datasets.ExpectedResultRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.datasets.ExpectedResultResponse;

import java.time.Instant;
import java.util.List;

public final class ExpectedResultExtensions {

    private ExpectedResultExtensions() {
    }

    public static ExpectedResultDto toDto(ExpectedResultRequest request, Task task, DatasetVersion version, TestStep step) {
        return new ExpectedResultDto(
                task,
                version,
                step,
                request.hash(),
                Instant.now()
        );
    }

    public static ExpectedResultResponse toResponse(ExpectedResult expectedResult) {
        return new ExpectedResultResponse(
                expectedResult.getId(),
                expectedResult.getTask().getId(),
                expectedResult.getVersion().getId(),
                expectedResult.getStep() == null ? null : expectedResult.getStep().getId(),
                expectedResult.getHash(),
                expectedResult.getComputedAt()
        );
    }

    public static List<ExpectedResultResponse> toResponse(List<ExpectedResult> expectedResults) {
        return expectedResults.stream()
                .map(ExpectedResultExtensions::toResponse)
                .toList();
    }
}
