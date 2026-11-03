package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskObjectFieldsInvalidException extends RuleViolationException {

    public TaskObjectFieldsInvalidException(String reason) {
        super(String.format("Task object fields are invalid: %s", reason));
    }
}
