package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TestStepTaskImmutableException extends RuleViolationException {

    public TestStepTaskImmutableException(String id) {
        super(String.format("Test step with id %s cannot move to another task.", id));
    }
}
