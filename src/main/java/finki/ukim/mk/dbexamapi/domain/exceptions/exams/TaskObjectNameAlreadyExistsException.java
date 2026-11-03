package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class TaskObjectNameAlreadyExistsException extends AlreadyExistsException {

    public TaskObjectNameAlreadyExistsException(String examId, String name) {
        super(String.format("An object named %s is already visible to the same group in exam with id %s.", name, examId));
    }
}
