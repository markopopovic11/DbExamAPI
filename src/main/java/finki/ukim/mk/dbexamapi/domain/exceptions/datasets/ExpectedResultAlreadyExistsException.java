package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class ExpectedResultAlreadyExistsException extends AlreadyExistsException {

    public ExpectedResultAlreadyExistsException(String taskId, String versionId) {
        super(String.format("Expected result for task with id %s on version with id %s already exists.", taskId, versionId));
    }
}
