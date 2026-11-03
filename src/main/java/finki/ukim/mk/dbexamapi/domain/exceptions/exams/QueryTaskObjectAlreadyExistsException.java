package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class QueryTaskObjectAlreadyExistsException extends AlreadyExistsException {

    public QueryTaskObjectAlreadyExistsException(String taskId) {
        super(String.format("QUERY task with id %s already has its view.", taskId));
    }
}
