package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class TaskVersionAlreadyExistsException extends AlreadyExistsException {

    public TaskVersionAlreadyExistsException(String taskId, String versionId) {
        super(String.format("Task version for task with id %s and version with id %s already exists.", taskId, versionId));
    }
}
