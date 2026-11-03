package finki.ukim.mk.dbexamapi.service.datasets.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.ExpectedResultDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultStepInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultVersionMismatchException;
import finki.ukim.mk.dbexamapi.domain.models.datasets.ExpectedResult;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.repository.datasets.ExpectedResultRepository;
import finki.ukim.mk.dbexamapi.service.datasets.ExpectedResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ExpectedResultServiceImpl implements ExpectedResultService {

    private final ExpectedResultRepository expectedResultRepository;

    public ExpectedResultServiceImpl(ExpectedResultRepository expectedResultRepository) {
        this.expectedResultRepository = expectedResultRepository;
    }

    @Override
    public Optional<ExpectedResult> findById(String id) {
        return expectedResultRepository.findById(id);
    }

    @Override
    public ExpectedResult findByIdNotNull(String id) {
        return expectedResultRepository.findById(id)
                .orElseThrow(() -> new ExpectedResultDoesNotExistException(id));
    }

    @Override
    public List<ExpectedResult> findAll() {
        return expectedResultRepository.findAll();
    }

    @Override
    @Transactional
    public ExpectedResult create(ExpectedResultDto expectedResultDto) {
        validate(expectedResultDto, null);

        ExpectedResult expectedResult = new ExpectedResult();
        expectedResult.setTask(expectedResultDto.task());
        expectedResult.setVersion(expectedResultDto.version());
        expectedResult.setStep(expectedResultDto.step());
        expectedResult.setHash(expectedResultDto.hash());
        expectedResult.setComputedAt(expectedResultDto.computedAt());

        ExpectedResult saved = expectedResultRepository.save(expectedResult);
        log.info("Created expected result with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExpectedResult update(String id, ExpectedResultDto expectedResultDto) {
        ExpectedResult expectedResult = findByIdNotNull(id);

        validate(expectedResultDto, id);

        expectedResult.setTask(expectedResultDto.task());
        expectedResult.setVersion(expectedResultDto.version());
        expectedResult.setStep(expectedResultDto.step());
        expectedResult.setHash(expectedResultDto.hash());
        expectedResult.setComputedAt(expectedResultDto.computedAt());

        ExpectedResult saved = expectedResultRepository.save(expectedResult);
        log.info("Updated expected result with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExpectedResult deleteById(String id) {
        ExpectedResult expectedResult = findByIdNotNull(id);
        expectedResultRepository.delete(expectedResult);
        log.info("Deleted expected result with id: {}", id);
        return expectedResult;
    }

    @Override
    @Transactional
    public void deleteAllByTaskId(String taskId) {
        expectedResultRepository.deleteAllByTask_Id(taskId);
    }

    @Override
    @Transactional
    public void deleteAllByTaskIdAndVersionId(String taskId, String versionId) {
        expectedResultRepository.deleteAllByTask_IdAndVersion_Id(taskId, versionId);
    }

    @Override
    @Transactional
    public void deleteAllByStepId(String stepId) {
        expectedResultRepository.deleteAllByStep_Id(stepId);
    }

    @Override
    @Transactional
    public void deleteAllByStepTaskId(String taskId) {
        expectedResultRepository.deleteAllByStep_Task_Id(taskId);
    }

    private void validate(ExpectedResultDto expectedResultDto, String id) {
        Task task = expectedResultDto.task();
        String taskId = task.getId();
        String versionId = expectedResultDto.version().getId();

        if (task.getDataset() == null
                || !expectedResultDto.version().getDataset().getId().equals(task.getDataset().getId())) {
            throw new ExpectedResultVersionMismatchException(taskId, versionId);
        }

        if (task.getType() == TaskType.QUERY) {
            if (expectedResultDto.step() != null) {
                throw new ExpectedResultStepInvalidException("QUERY results have no step");
            }
            boolean exists = id == null
                    ? expectedResultRepository.existsByTask_IdAndVersion_IdAndStepIsNull(taskId, versionId)
                    : expectedResultRepository.existsByTask_IdAndVersion_IdAndStepIsNullAndIdNot(taskId, versionId, id);
            if (exists) {
                throw new ExpectedResultAlreadyExistsException(taskId, versionId);
            }
            return;
        }

        if (expectedResultDto.step() == null || !expectedResultDto.step().getTask().getId().equals(taskId)) {
            throw new ExpectedResultStepInvalidException("CODE results need a step of the same task");
        }
        String stepId = expectedResultDto.step().getId();
        boolean exists = id == null
                ? expectedResultRepository.existsByTask_IdAndVersion_IdAndStep_Id(taskId, versionId, stepId)
                : expectedResultRepository.existsByTask_IdAndVersion_IdAndStep_IdAndIdNot(taskId, versionId, stepId, id);
        if (exists) {
            throw new ExpectedResultAlreadyExistsException(taskId, versionId);
        }
    }
}
