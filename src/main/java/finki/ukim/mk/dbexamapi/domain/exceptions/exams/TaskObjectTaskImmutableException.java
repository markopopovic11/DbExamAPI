package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskObjectTaskImmutableException extends RuleViolationException {

    public TaskObjectTaskImmutableException(String id) {
        super(String.format("Task object with id %s cannot move to another task.", id));
    }
}
