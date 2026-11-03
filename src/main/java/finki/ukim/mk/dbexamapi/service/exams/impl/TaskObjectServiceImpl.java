package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskObjectDto;
import finki.ukim.mk.dbexamapi.domain.enums.TaskObjectKind;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.QueryTaskObjectAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectFieldsInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectKindInvalidException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectNameAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.TaskObjectTaskImmutableException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TaskObject;
import finki.ukim.mk.dbexamapi.repository.exams.TaskObjectRepository;
import finki.ukim.mk.dbexamapi.service.exams.TaskObjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskObjectServiceImpl implements TaskObjectService {

    private final TaskObjectRepository taskObjectRepository;

    public TaskObjectServiceImpl(TaskObjectRepository taskObjectRepository) {
        this.taskObjectRepository = taskObjectRepository;
    }

    @Override
    public Optional<TaskObject> findById(String id) {
        return taskObjectRepository.findById(id);
    }

    @Override
    public TaskObject findByIdNotNull(String id) {
        return taskObjectRepository.findById(id)
                .orElseThrow(() -> new TaskObjectDoesNotExistException(id));
    }

    @Override
    public List<TaskObject> findAll() {
        return taskObjectRepository.findAll();
    }

    @Override
    @Transactional
    public TaskObject create(TaskObjectDto taskObjectDto) {
        Task task = taskObjectDto.task();
        if (task.getType() == TaskType.QUERY && taskObjectRepository.existsByTask_Id(task.getId())) {
            throw new QueryTaskObjectAlreadyExistsException(task.getId());
        }
        validate(taskObjectDto, null);

        TaskObject taskObject = new TaskObject();
        taskObject.setTask(task);
        taskObject.setKind(taskObjectDto.kind());
        taskObject.setName(taskObjectDto.name());
        taskObject.setArguments(taskObjectDto.arguments());
        taskObject.setOnTable(taskObjectDto.onTable());
        taskObject.setSortOrder(taskObjectDto.sortOrder());

        TaskObject saved = taskObjectRepository.save(taskObject);
        log.info("Created task object with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TaskObject update(String id, TaskObjectDto taskObjectDto) {
        TaskObject taskObject = findByIdNotNull(id);

        if (!taskObjectDto.task().getId().equals(taskObject.getTask().getId())) {
            throw new TaskObjectTaskImmutableException(id);
        }
        validate(taskObjectDto, id);

        taskObject.setKind(taskObjectDto.kind());
        taskObject.setName(taskObjectDto.name());
        taskObject.setArguments(taskObjectDto.arguments());
        taskObject.setOnTable(taskObjectDto.onTable());
        taskObject.setSortOrder(taskObjectDto.sortOrder());

        TaskObject saved = taskObjectRepository.save(taskObject);
        log.info("Updated task object with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TaskObject deleteById(String id) {
        TaskObject taskObject = findByIdNotNull(id);
        taskObjectRepository.delete(taskObject);
        log.info("Deleted task object with id: {}", id);
        return taskObject;
    }

    @Override
    @Transactional
    public void deleteAllByTaskId(String taskId) {
        taskObjectRepository.deleteAllByTask_Id(taskId);
        log.info("Deleted task objects of task with id: {}", taskId);
    }

    private void validate(TaskObjectDto taskObjectDto, String id) {
        Task task = taskObjectDto.task();
        TaskObjectKind kind = taskObjectDto.kind();

        boolean kindFits = switch (task.getType()) {
            case QUERY -> kind == TaskObjectKind.VIEW;
            case CODE -> kind != TaskObjectKind.VIEW;
            default -> false;
        };
        if (!kindFits) {
            throw new TaskObjectKindInvalidException(task.getType().name(), kind.name());
        }

        boolean takesArguments = kind == TaskObjectKind.FUNCTION || kind == TaskObjectKind.PROCEDURE;
        if (!takesArguments && taskObjectDto.arguments() != null) {
            throw new TaskObjectFieldsInvalidException("only functions and procedures have arguments");
        }
        if ((kind == TaskObjectKind.TRIGGER) != (taskObjectDto.onTable() != null)) {
            throw new TaskObjectFieldsInvalidException("a trigger needs its table and other objects have none");
        }

        String examId = task.getExam().getId();
        String groupId = task.getGroup() == null ? null : task.getGroup().getId();
        if (taskObjectRepository.existsNameInGroupView(examId, groupId, taskObjectDto.name(), id)) {
            throw new TaskObjectNameAlreadyExistsException(examId, taskObjectDto.name());
        }
    }
}
