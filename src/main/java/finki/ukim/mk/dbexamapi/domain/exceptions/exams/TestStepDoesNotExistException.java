package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class TestStepDoesNotExistException extends NotFoundException {

    public TestStepDoesNotExistException(String id) {
        super(String.format("Test step with id %s does not exist.", id));
    }
}
