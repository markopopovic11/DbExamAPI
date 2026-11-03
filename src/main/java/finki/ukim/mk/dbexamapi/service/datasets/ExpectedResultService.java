package finki.ukim.mk.dbexamapi.service.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.ExpectedResultDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.ExpectedResult;

import java.util.List;
import java.util.Optional;

public interface ExpectedResultService {

    Optional<ExpectedResult> findById(String id);

    ExpectedResult findByIdNotNull(String id);

    List<ExpectedResult> findAll();

    ExpectedResult create(ExpectedResultDto expectedResultDto);

    ExpectedResult update(String id, ExpectedResultDto expectedResultDto);

    ExpectedResult deleteById(String id);

    void deleteAllByTaskId(String taskId);

    void deleteAllByTaskIdAndVersionId(String taskId, String versionId);

    void deleteAllByStepId(String stepId);

    void deleteAllByStepTaskId(String taskId);
}
