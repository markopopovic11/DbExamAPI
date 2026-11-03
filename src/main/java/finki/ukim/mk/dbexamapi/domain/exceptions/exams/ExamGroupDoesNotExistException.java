package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class ExamGroupDoesNotExistException extends NotFoundException {

    public ExamGroupDoesNotExistException(String id) {
        super(String.format("Exam group with id %s does not exist.", id));
    }
}
