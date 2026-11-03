package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class ExamEndsBeforeStartException extends RuleViolationException {

    public ExamEndsBeforeStartException() {
        super("Exam must end after it starts.");
    }
}
