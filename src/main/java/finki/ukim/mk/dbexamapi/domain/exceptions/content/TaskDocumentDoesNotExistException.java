package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class TaskDocumentDoesNotExistException extends NotFoundException {

    public TaskDocumentDoesNotExistException(String id) {
        super(String.format("Task document with id %s does not exist.", id));
    }
}
