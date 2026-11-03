package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class DocumentFileDoesNotExistException extends NotFoundException {

    public DocumentFileDoesNotExistException(String id) {
        super(String.format("Document with id %s has no file.", id));
    }
}
