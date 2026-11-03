package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskDataSourceInvalidException extends RuleViolationException {

    public TaskDataSourceInvalidException(String reason) {
        super(String.format("Task data source is invalid: %s", reason));
    }
}
