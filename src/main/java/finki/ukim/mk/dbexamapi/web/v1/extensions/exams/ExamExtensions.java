package finki.ukim.mk.dbexamapi.web.v1.extensions.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.ExamDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.ExamRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.ExamResponse;

import java.util.List;

public final class ExamExtensions {

    private ExamExtensions() {
    }

    public static ExamDto toDto(ExamRequest request, Course course) {
        return new ExamDto(
                course,
                request.name(),
                request.startsAt(),
                request.endsAt()
        );
    }

    public static ExamResponse toResponse(Exam exam) {
        return new ExamResponse(
                exam.getId(),
                exam.getCourse().getId(),
                exam.getName(),
                exam.getStartsAt(),
                exam.getEndsAt()
        );
    }

    public static List<ExamResponse> toResponse(List<Exam> exams) {
        return exams.stream()
                .map(ExamExtensions::toResponse)
                .toList();
    }
}
