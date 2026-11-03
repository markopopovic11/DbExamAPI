package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class DatasetVersionDocumentInvalidException extends RuleViolationException {

    public DatasetVersionDocumentInvalidException(String documentId) {
        super(String.format("Document with id %s must be an uploaded file in no folder to hold a version's data.", documentId));
    }
}
