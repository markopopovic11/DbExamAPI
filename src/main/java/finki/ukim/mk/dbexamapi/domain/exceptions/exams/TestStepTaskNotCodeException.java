package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TestStepTaskNotCodeException extends RuleViolationException {

    public TestStepTaskNotCodeException(String taskId) {
        super(String.format("Task with id %s is not a CODE task; only CODE tasks have test steps.", taskId));
    }
}
