package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskExamImmutableException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskInUseException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskSortOrderAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.repository.exams.TaskRepository;
import finki.ukim.mk.dbexamapi.service.content.TaskDocumentService;
import finki.ukim.mk.dbexamapi.service.datasets.TaskVersionService;
import finki.ukim.mk.dbexamapi.service.exams.TaskObjectService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.service.exams.TestStepService;
import finki.ukim.mk.dbexamapi.helpers.exams.TaskRuleHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskDocumentService taskDocumentService;
    private final TaskObjectService taskObjectService;
    private final TestStepService testStepService;
    private final TaskVersionService taskVersionService;
    private final TaskRuleHelper taskRuleHelper;

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskDocumentService taskDocumentService,
                           TaskObjectService taskObjectService,
                           TestStepService testStepService,
                           TaskVersionService taskVersionService,
                           TaskRuleHelper taskRuleHelper) {
        this.taskRepository = taskRepository;
        this.taskDocumentService = taskDocumentService;
        this.taskObjectService = taskObjectService;
        this.testStepService = testStepService;
        this.taskVersionService = taskVersionService;
        this.taskRuleHelper = taskRuleHelper;
    }

    @Override
    public Optional<Task> findById(String id) {
        return taskRepository.findById(id);
    }

    @Override
    public Task findByIdNotNull(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskDoesNotExistException(id));
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    @Transactional
    public Task create(TaskDto taskDto) {
        taskRuleHelper.requireGroupInExam(taskDto);
        taskRuleHelper.requireDataSourceFits(taskDto);
        requireSortOrderFree(taskDto, null);

        Task task = new Task();
        task.setExam(taskDto.exam());
        task.setGroup(taskDto.group());
        task.setType(taskDto.type());
        task.setDataSource(taskDto.dataSource());
        task.setSourceTask(taskDto.sourceTask());
        task.setDataset(taskDto.dataset());
        task.setSolution(taskDto.solution());
        task.setTitle(taskDto.title());
        task.setDescription(taskDto.description());
        task.setPoints(taskDto.points());
        task.setSortOrder(taskDto.sortOrder());

        Task saved = taskRepository.save(task);
        log.info("Created task with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Task update(String id, TaskDto taskDto) {
        Task task = findByIdNotNull(id);

        if (!taskDto.exam().getId().equals(task.getExam().getId())) {
            throw new TaskExamImmutableException(id);
        }
        taskRuleHelper.requireGroupInExam(taskDto);
        taskRuleHelper.requireDataSourceFits(taskDto);
        requireSortOrderFree(taskDto, id);

        task.setGroup(taskDto.group());
        task.setType(taskDto.type());
        task.setDataSource(taskDto.dataSource());
        task.setSourceTask(taskDto.sourceTask());
        task.setDataset(taskDto.dataset());
        task.setSolution(taskDto.solution());
        task.setTitle(taskDto.title());
        task.setDescription(taskDto.description());
        task.setPoints(taskDto.points());
        task.setSortOrder(taskDto.sortOrder());

        Task saved = taskRepository.save(task);
        log.info("Updated task with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Task deleteById(String id) {
        Task task = findByIdNotNull(id);
        if (taskRepository.existsBySourceTask_Id(id)) {
            throw new TaskInUseException(id);
        }

        taskVersionService.deleteAllByTaskId(id);
        testStepService.deleteAllByTaskId(id);
        taskObjectService.deleteAllByTaskId(id);
        taskDocumentService.deleteAllByTaskId(id);
        taskRepository.delete(task);
        log.info("Deleted task with id: {} and everything that belongs to it", id);
        return task;
    }

    @Override
    public boolean existsByGroupId(String groupId) {
        return taskRepository.existsByGroup_Id(groupId);
    }

    @Override
    public boolean existsByDatasetId(String datasetId) {
        return taskRepository.existsByDataset_Id(datasetId);
    }

    // Unique among the tasks one group sees: its own plus the shared ones. id is null on create.
    private void requireSortOrderFree(TaskDto taskDto, String id) {
        String examId = taskDto.exam().getId();
        String groupId = taskDto.group() == null ? null : taskDto.group().getId();
        if (taskRepository.existsSortOrderInGroupView(examId, groupId, taskDto.sortOrder(), id)) {
            throw new TaskSortOrderAlreadyExistsException(examId, taskDto.sortOrder());
        }
    }
}
