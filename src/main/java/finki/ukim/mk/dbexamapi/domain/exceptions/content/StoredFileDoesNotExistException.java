package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class StoredFileDoesNotExistException extends NotFoundException {

    public StoredFileDoesNotExistException(String filePath) {
        super(String.format("No stored file at %s.", filePath));
    }
}
