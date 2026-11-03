package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class TaskSortOrderAlreadyExistsException extends AlreadyExistsException {

    public TaskSortOrderAlreadyExistsException(String examId, int sortOrder) {
        super(String.format("A task with sort order %d is already visible to the same group in exam with id %s.", sortOrder, examId));
    }
}
