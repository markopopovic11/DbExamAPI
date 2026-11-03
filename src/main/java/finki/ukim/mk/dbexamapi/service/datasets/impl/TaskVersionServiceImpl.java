package finki.ukim.mk.dbexamapi.service.datasets.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.TaskVersionDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.TaskVersionAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.datasets.TaskVersionDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.datasets.TaskVersion;
import finki.ukim.mk.dbexamapi.repository.datasets.TaskVersionRepository;
import finki.ukim.mk.dbexamapi.service.datasets.ExpectedResultService;
import finki.ukim.mk.dbexamapi.service.datasets.TaskVersionService;
import finki.ukim.mk.dbexamapi.helpers.datasets.DatasetVersionRuleHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskVersionServiceImpl implements TaskVersionService {

    private final TaskVersionRepository taskVersionRepository;
    private final ExpectedResultService expectedResultService;
    private final DatasetVersionRuleHelper datasetVersionRuleHelper;

    public TaskVersionServiceImpl(TaskVersionRepository taskVersionRepository,
                                  ExpectedResultService expectedResultService,
                                  DatasetVersionRuleHelper datasetVersionRuleHelper) {
        this.taskVersionRepository = taskVersionRepository;
        this.expectedResultService = expectedResultService;
        this.datasetVersionRuleHelper = datasetVersionRuleHelper;
    }

    @Override
    public Optional<TaskVersion> findById(String id) {
        return taskVersionRepository.findById(id);
    }

    @Override
    public TaskVersion findByIdNotNull(String id) {
        return taskVersionRepository.findById(id)
                .orElseThrow(() -> new TaskVersionDoesNotExistException(id));
    }

    @Override
    public List<TaskVersion> findAll() {
        return taskVersionRepository.findAll();
    }

    @Override
    @Transactional
    public TaskVersion create(TaskVersionDto taskVersionDto) {
        datasetVersionRuleHelper.requireVersionOfTaskDataset(taskVersionDto.task(), taskVersionDto.version());

        String taskId = taskVersionDto.task().getId();
        String versionId = taskVersionDto.version().getId();
        if (taskVersionRepository.existsByTask_IdAndVersion_Id(taskId, versionId)) {
            throw new TaskVersionAlreadyExistsException(taskId, versionId);
        }

        TaskVersion taskVersion = new TaskVersion();
        taskVersion.setTask(taskVersionDto.task());
        taskVersion.setVersion(taskVersionDto.version());

        TaskVersion saved = taskVersionRepository.save(taskVersion);
        log.info("Created task version with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TaskVersion update(String id, TaskVersionDto taskVersionDto) {
        TaskVersion taskVersion = findByIdNotNull(id);

        datasetVersionRuleHelper.requireVersionOfTaskDataset(taskVersionDto.task(), taskVersionDto.version());

        String taskId = taskVersionDto.task().getId();
        String versionId = taskVersionDto.version().getId();
        if (taskVersionRepository.existsByTask_IdAndVersion_IdAndIdNot(taskId, versionId, id)) {
            throw new TaskVersionAlreadyExistsException(taskId, versionId);
        }

        expectedResultService.deleteAllByTaskIdAndVersionId(
                taskVersion.getTask().getId(), taskVersion.getVersion().getId());

        taskVersion.setTask(taskVersionDto.task());
        taskVersion.setVersion(taskVersionDto.version());

        TaskVersion saved = taskVersionRepository.save(taskVersion);
        log.info("Updated task version with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TaskVersion deleteById(String id) {
        TaskVersion taskVersion = findByIdNotNull(id);
        expectedResultService.deleteAllByTaskIdAndVersionId(
                taskVersion.getTask().getId(), taskVersion.getVersion().getId());
        taskVersionRepository.delete(taskVersion);
        log.info("Deleted task version with id: {} and its expected results", id);
        return taskVersion;
    }

    @Override
    public boolean existsByVersionId(String versionId) {
        return taskVersionRepository.existsByVersion_Id(versionId);
    }

    @Override
    @Transactional
    public void deleteAllByTaskId(String taskId) {
        expectedResultService.deleteAllByTaskId(taskId);
        taskVersionRepository.deleteAllByTask_Id(taskId);
        log.info("Deleted task versions of task with id: {} and its expected results", taskId);
    }
}
