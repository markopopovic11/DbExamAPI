package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class ExamGroupExamImmutableException extends RuleViolationException {

    public ExamGroupExamImmutableException(String id) {
        super(String.format("Exam group with id %s cannot move to another exam.", id));
    }
}
