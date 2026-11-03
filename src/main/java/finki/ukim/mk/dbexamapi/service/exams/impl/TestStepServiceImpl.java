package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TestStepDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepCheckRequiredException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepSortOrderAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepTaskImmutableException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TestStepTaskNotCodeException;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;
import finki.ukim.mk.dbexamapi.repository.exams.TestStepRepository;
import finki.ukim.mk.dbexamapi.service.datasets.ExpectedResultService;
import finki.ukim.mk.dbexamapi.service.exams.TestStepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TestStepServiceImpl implements TestStepService {

    private final TestStepRepository testStepRepository;
    private final ExpectedResultService expectedResultService;

    public TestStepServiceImpl(TestStepRepository testStepRepository, ExpectedResultService expectedResultService) {
        this.testStepRepository = testStepRepository;
        this.expectedResultService = expectedResultService;
    }

    @Override
    public Optional<TestStep> findById(String id) {
        return testStepRepository.findById(id);
    }

    @Override
    public TestStep findByIdNotNull(String id) {
        return testStepRepository.findById(id)
                .orElseThrow(() -> new TestStepDoesNotExistException(id));
    }

    @Override
    public List<TestStep> findAll() {
        return testStepRepository.findAll();
    }

    @Override
    @Transactional
    public TestStep create(TestStepDto testStepDto) {
        validateTaskAndCheck(testStepDto);

        String taskId = testStepDto.task().getId();
        if (testStepRepository.existsByTask_IdAndSortOrder(taskId, testStepDto.sortOrder())) {
            throw new TestStepSortOrderAlreadyExistsException(taskId, testStepDto.sortOrder());
        }

        TestStep testStep = new TestStep();
        testStep.setTask(testStepDto.task());
        testStep.setSortOrder(testStepDto.sortOrder());
        testStep.setActionSql(testStepDto.actionSql());
        testStep.setExpectError(testStepDto.expectError());
        testStep.setCheckSql(testStepDto.checkSql());

        TestStep saved = testStepRepository.save(testStep);
        log.info("Created test step with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TestStep update(String id, TestStepDto testStepDto) {
        TestStep testStep = findByIdNotNull(id);

        String taskId = testStep.getTask().getId();
        if (!testStepDto.task().getId().equals(taskId)) {
            throw new TestStepTaskImmutableException(id);
        }
        validateTaskAndCheck(testStepDto);

        if (testStepRepository.existsByTask_IdAndSortOrderAndIdNot(taskId, testStepDto.sortOrder(), id)) {
            throw new TestStepSortOrderAlreadyExistsException(taskId, testStepDto.sortOrder());
        }

        testStep.setSortOrder(testStepDto.sortOrder());
        testStep.setActionSql(testStepDto.actionSql());
        testStep.setExpectError(testStepDto.expectError());
        testStep.setCheckSql(testStepDto.checkSql());

        TestStep saved = testStepRepository.save(testStep);
        log.info("Updated test step with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TestStep deleteById(String id) {
        TestStep testStep = findByIdNotNull(id);
        expectedResultService.deleteAllByStepId(id);
        testStepRepository.delete(testStep);
        log.info("Deleted test step with id: {} and its expected results", id);
        return testStep;
    }

    @Override
    @Transactional
    public void deleteAllByTaskId(String taskId) {
        expectedResultService.deleteAllByStepTaskId(taskId);
        testStepRepository.deleteAllByTask_Id(taskId);
        log.info("Deleted test steps of task with id: {} and their expected results", taskId);
    }

    private void validateTaskAndCheck(TestStepDto testStepDto) {
        if (testStepDto.task().getType() != TaskType.CODE) {
            throw new TestStepTaskNotCodeException(testStepDto.task().getId());
        }
        if (testStepDto.checkSql() == null && !testStepDto.expectError()) {
            throw new TestStepCheckRequiredException();
        }
    }
}
