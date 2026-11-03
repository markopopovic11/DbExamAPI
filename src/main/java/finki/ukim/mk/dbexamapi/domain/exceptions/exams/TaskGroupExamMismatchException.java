package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class TaskGroupExamMismatchException extends RuleViolationException {

    public TaskGroupExamMismatchException(String groupId, String examId) {
        super(String.format("Exam group with id %s does not belong to exam with id %s.", groupId, examId));
    }
}
