package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class ExpectedResultStepInvalidException extends RuleViolationException {

    public ExpectedResultStepInvalidException(String reason) {
        super(String.format("Expected result step is invalid: %s", reason));
    }
}
