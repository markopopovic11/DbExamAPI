package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class ExamGroupNameAlreadyExistsException extends AlreadyExistsException {

    public ExamGroupNameAlreadyExistsException(String examId, String name) {
        super(String.format("Exam group named %s in exam with id %s already exists.", name, examId));
    }
}
