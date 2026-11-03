package finki.ukim.mk.dbexamapi.service.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TestStepDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;

import java.util.List;
import java.util.Optional;

public interface TestStepService {

    Optional<TestStep> findById(String id);

    TestStep findByIdNotNull(String id);

    List<TestStep> findAll();

    TestStep create(TestStepDto testStepDto);

    TestStep update(String id, TestStepDto testStepDto);

    /**
     * Also deletes the expected results of the step.
     */
    TestStep deleteById(String id);

    /**
     * Also deletes the expected results of the steps.
     */
    void deleteAllByTaskId(String taskId);
}
