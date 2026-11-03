package finki.ukim.mk.dbexamapi.domain.exceptions.content;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class DocumentSourceInvalidException extends RuleViolationException {

    public DocumentSourceInvalidException() {
        super("A document needs exactly one of markdown content, a file or a link.");
    }
}
