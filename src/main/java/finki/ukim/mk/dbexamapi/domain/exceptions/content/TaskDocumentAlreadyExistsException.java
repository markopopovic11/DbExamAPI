package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class TaskDocumentAlreadyExistsException extends AlreadyExistsException {

    public TaskDocumentAlreadyExistsException(String taskId, String documentId) {
        super(String.format("Task document for task with id %s and document with id %s already exists.", taskId, documentId));
    }
}
