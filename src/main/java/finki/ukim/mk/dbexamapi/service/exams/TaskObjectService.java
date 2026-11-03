package finki.ukim.mk.dbexamapi.service.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.TaskObjectDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.TaskObject;

import java.util.List;
import java.util.Optional;

public interface TaskObjectService {

    Optional<TaskObject> findById(String id);

    TaskObject findByIdNotNull(String id);

    List<TaskObject> findAll();

    TaskObject create(TaskObjectDto taskObjectDto);

    TaskObject update(String id, TaskObjectDto taskObjectDto);

    TaskObject deleteById(String id);

    void deleteAllByTaskId(String taskId);
}
