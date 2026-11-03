package finki.ukim.mk.dbexamapi.helpers.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.ExamDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.CourseInactiveException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.ExamEndsBeforeStartException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import org.springframework.stereotype.Component;

/**
 * Rules for an exam's course and time window.
 */
@Component
public class ExamRuleHelper {

    // An inactive course takes no new exams; its old exams stay as history.
    public void requireCourseActive(Course course) {
        if (!course.isActive()) {
            throw new CourseInactiveException(course.getId());
        }
    }

    public void requireEndsAfterStart(ExamDto examDto) {
        if (!examDto.endsAt().isAfter(examDto.startsAt())) {
            throw new ExamEndsBeforeStartException();
        }
    }
}
