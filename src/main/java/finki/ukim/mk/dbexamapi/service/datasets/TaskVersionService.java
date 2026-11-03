package finki.ukim.mk.dbexamapi.service.datasets;

import finki.ukim.mk.dbexamapi.domain.dtos.datasets.TaskVersionDto;
import finki.ukim.mk.dbexamapi.domain.models.datasets.TaskVersion;

import java.util.List;
import java.util.Optional;

public interface TaskVersionService {

    Optional<TaskVersion> findById(String id);

    TaskVersion findByIdNotNull(String id);

    List<TaskVersion> findAll();

    TaskVersion create(TaskVersionDto taskVersionDto);

    /**
     * Also deletes the expected results of the old task and version pair.
     */
    TaskVersion update(String id, TaskVersionDto taskVersionDto);

    /**
     * Also deletes the expected results of the task on this version.
     */
    TaskVersion deleteById(String id);

    boolean existsByVersionId(String versionId);

    /**
     * Also deletes every expected result of the task.
     */
    void deleteAllByTaskId(String taskId);
}
