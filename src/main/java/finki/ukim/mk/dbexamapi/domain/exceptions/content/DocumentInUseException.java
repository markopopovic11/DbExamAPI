package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.InUseException;

public class DocumentInUseException extends InUseException {

    public DocumentInUseException(String id) {
        super(String.format("Document with id %s cannot be deleted while tasks show it.", id));
    }
}
