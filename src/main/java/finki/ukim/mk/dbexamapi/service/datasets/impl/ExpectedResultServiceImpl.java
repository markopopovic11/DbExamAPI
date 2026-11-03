package finki.ukim.mk.dbexamapi.service.datasets.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.ExpectedResultDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.ExpectedResultDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.datasets.ExpectedResult;
import finki.ukim.mk.dbexamapi.repository.datasets.ExpectedResultRepository;
import finki.ukim.mk.dbexamapi.service.datasets.ExpectedResultService;
import finki.ukim.mk.dbexamapi.helpers.datasets.DatasetVersionRuleHelper;
import finki.ukim.mk.dbexamapi.helpers.datasets.ExpectedResultRuleHelper;
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
    private final DatasetVersionRuleHelper datasetVersionRuleHelper;
    private final ExpectedResultRuleHelper expectedResultRuleHelper;

    public ExpectedResultServiceImpl(ExpectedResultRepository expectedResultRepository,
                                     DatasetVersionRuleHelper datasetVersionRuleHelper,
                                     ExpectedResultRuleHelper expectedResultRuleHelper) {
        this.expectedResultRepository = expectedResultRepository;
        this.datasetVersionRuleHelper = datasetVersionRuleHelper;
        this.expectedResultRuleHelper = expectedResultRuleHelper;
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
        datasetVersionRuleHelper.requireVersionOfTaskDataset(expectedResultDto.task(), expectedResultDto.version());
        expectedResultRuleHelper.requireStepFitsTask(expectedResultDto);
        requireNotDuplicate(expectedResultDto, null);

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

        datasetVersionRuleHelper.requireVersionOfTaskDataset(expectedResultDto.task(), expectedResultDto.version());
        expectedResultRuleHelper.requireStepFitsTask(expectedResultDto);
        requireNotDuplicate(expectedResultDto, id);

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

    // One result per task and version for QUERY, per task, version and step for CODE. id is null on create.
    private void requireNotDuplicate(ExpectedResultDto expectedResultDto, String id) {
        String taskId = expectedResultDto.task().getId();
        String versionId = expectedResultDto.version().getId();
        boolean exists = expectedResultDto.step() == null
                ? existsWithoutStep(taskId, versionId, id)
                : existsWithStep(taskId, versionId, expectedResultDto.step().getId(), id);
        if (exists) {
            throw new ExpectedResultAlreadyExistsException(taskId, versionId);
        }
    }

    private boolean existsWithoutStep(String taskId, String versionId, String id) {
        return id == null
                ? expectedResultRepository.existsByTask_IdAndVersion_IdAndStepIsNull(taskId, versionId)
                : expectedResultRepository.existsByTask_IdAndVersion_IdAndStepIsNullAndIdNot(taskId, versionId, id);
    }

    private boolean existsWithStep(String taskId, String versionId, String stepId, String id) {
        return id == null
                ? expectedResultRepository.existsByTask_IdAndVersion_IdAndStep_Id(taskId, versionId, stepId)
                : expectedResultRepository.existsByTask_IdAndVersion_IdAndStep_IdAndIdNot(taskId, versionId, stepId, id);
    }
}
