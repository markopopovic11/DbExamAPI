package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskDataSourceInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskExamImmutableException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskGroupExamMismatchException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskInUseException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskSortOrderAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.repository.exams.TaskRepository;
import finki.ukim.mk.dbexamapi.service.content.TaskDocumentService;
import finki.ukim.mk.dbexamapi.service.datasets.TaskVersionService;
import finki.ukim.mk.dbexamapi.service.exams.TaskObjectService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import finki.ukim.mk.dbexamapi.service.exams.TestStepService;
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

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskDocumentService taskDocumentService,
                           TaskObjectService taskObjectService,
                           TestStepService testStepService,
                           TaskVersionService taskVersionService) {
        this.taskRepository = taskRepository;
        this.taskDocumentService = taskDocumentService;
        this.taskObjectService = taskObjectService;
        this.testStepService = testStepService;
        this.taskVersionService = taskVersionService;
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
        validate(taskDto, null);

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
        validate(taskDto, id);

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

    private void validate(TaskDto taskDto, String id) {
        String examId = taskDto.exam().getId();
        String groupId = taskDto.group() == null ? null : taskDto.group().getId();

        if (taskDto.group() != null && !taskDto.group().getExam().getId().equals(examId)) {
            throw new TaskGroupExamMismatchException(groupId, examId);
        }
        validateDataSource(taskDto, examId);

        if (taskRepository.existsSortOrderInGroupView(examId, groupId, taskDto.sortOrder(), id)) {
            throw new TaskSortOrderAlreadyExistsException(examId, taskDto.sortOrder());
        }
    }

    private void validateDataSource(TaskDto taskDto, String examId) {
        boolean runsOnData = taskDto.type() == TaskType.QUERY || taskDto.type() == TaskType.CODE;

        if (!runsOnData) {
            if (taskDto.dataSource() != null || taskDto.sourceTask() != null || taskDto.dataset() != null) {
                throw new TaskDataSourceInvalidException("only QUERY and CODE tasks have a data source");
            }
            return;
        }
        if (taskDto.dataSource() == null) {
            throw new TaskDataSourceInvalidException("QUERY and CODE tasks need a data source");
        }

        switch (taskDto.dataSource()) {
            case DATASETS -> {
                if (taskDto.dataset() == null || taskDto.sourceTask() != null) {
                    throw new TaskDataSourceInvalidException("DATASETS needs a dataset and no source task");
                }
                if (!taskDto.dataset().getExam().getId().equals(examId)) {
                    throw new TaskDataSourceInvalidException("the dataset must belong to the task's exam");
                }
            }
            case OWN_SCHEMA -> {
                if (taskDto.sourceTask() == null || taskDto.dataset() != null) {
                    throw new TaskDataSourceInvalidException("OWN_SCHEMA needs a source task and no dataset");
                }
                if (taskDto.sourceTask().getType() != TaskType.DDL) {
                    throw new TaskDataSourceInvalidException("the source task must be a DDL task");
                }
                if (!taskDto.sourceTask().getExam().getId().equals(examId)) {
                    throw new TaskDataSourceInvalidException("the source task must belong to the task's exam");
                }
            }
        }
    }
}
