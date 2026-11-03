package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class DocumentFileEmptyException extends RuleViolationException {

    public DocumentFileEmptyException() {
        super("The uploaded file is empty.");
    }
}
