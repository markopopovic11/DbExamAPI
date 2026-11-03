package finki.ukim.mk.dbexamapi.service.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    Optional<Task> findById(String id);

    Task findByIdNotNull(String id);

    List<Task> findAll();

    Task create(TaskDto taskDto);

    Task update(String id, TaskDto taskDto);

    /**
     * Also deletes what belongs to the task: its document links, objects, test steps,
     * versions and expected results.
     */
    Task deleteById(String id);

    boolean existsByGroupId(String groupId);

    boolean existsByDatasetId(String datasetId);
}
