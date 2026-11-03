package finki.ukim.mk.dbexamapi.domain.exceptions.exams;

import finki.ukim.mk.dbexamapi.domain.exceptions.RuleViolationException;

public class CourseInactiveException extends RuleViolationException {

    public CourseInactiveException(String courseId) {
        super(String.format("Course with id %s is inactive; no new exams can be added to it.", courseId));
    }
}
