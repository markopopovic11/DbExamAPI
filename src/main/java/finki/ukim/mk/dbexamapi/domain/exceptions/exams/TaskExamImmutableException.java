package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskExamImmutableException extends RuleViolationException {

    public TaskExamImmutableException(String id) {
        super(String.format("Task with id %s cannot move to another exam.", id));
    }
}
