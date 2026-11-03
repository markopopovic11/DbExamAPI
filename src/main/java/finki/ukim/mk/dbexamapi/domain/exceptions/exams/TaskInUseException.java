package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.InUseException;

public class TaskInUseException extends InUseException {

    public TaskInUseException(String id) {
        super(String.format("Task with id %s cannot be deleted while other tasks run on its schema.", id));
    }
}
