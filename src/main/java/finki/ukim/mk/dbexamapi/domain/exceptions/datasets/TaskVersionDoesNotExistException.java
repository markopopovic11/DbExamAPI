package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class TaskVersionDoesNotExistException extends NotFoundException {

    public TaskVersionDoesNotExistException(String id) {
        super(String.format("Task version with id %s does not exist.", id));
    }
}
