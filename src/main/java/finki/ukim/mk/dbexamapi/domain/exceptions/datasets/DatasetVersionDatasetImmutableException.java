package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class DatasetVersionDatasetImmutableException extends RuleViolationException {

    public DatasetVersionDatasetImmutableException(String id) {
        super(String.format("Dataset version with id %s cannot move to another dataset.", id));
    }
}
