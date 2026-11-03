package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class TaskObjectDoesNotExistException extends NotFoundException {

    public TaskObjectDoesNotExistException(String id) {
        super(String.format("Task object with id %s does not exist.", id));
    }
}
