package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class ExpectedResultDoesNotExistException extends NotFoundException {

    public ExpectedResultDoesNotExistException(String id) {
        super(String.format("Expected result with id %s does not exist.", id));
    }
}
