package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.InUseException;

public class ExamGroupInUseException extends InUseException {

    public ExamGroupInUseException(String id) {
        super(String.format("Exam group with id %s cannot be deleted while tasks belong to it.", id));
    }
}
