package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class DatasetExamImmutableException extends RuleViolationException {

    public DatasetExamImmutableException(String id) {
        super(String.format("Dataset with id %s cannot move to another exam.", id));
    }
}
