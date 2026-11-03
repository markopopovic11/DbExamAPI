package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TestStepCheckRequiredException extends RuleViolationException {

    public TestStepCheckRequiredException() {
        super("A test step needs a check unless its action is expected to fail.");
    }
}
