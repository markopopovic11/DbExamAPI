package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class TestStepSortOrderAlreadyExistsException extends AlreadyExistsException {

    public TestStepSortOrderAlreadyExistsException(String taskId, int sortOrder) {
        super(String.format("Test step with sort order %d in task with id %s already exists.", sortOrder, taskId));
    }
}
