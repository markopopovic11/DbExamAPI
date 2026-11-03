package finki.ukim.mk.dbexamapi.web.v1.mappers.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import finki.ukim.mk.dbexamapi.service.exams.CourseService;
import finki.ukim.mk.dbexamapi.service.exams.ExamService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.exams.ExamExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.ExamRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.ExamResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamMapper {

    private final ExamService examService;
    private final CourseService courseService;

    public ExamMapper(ExamService examService,
                      CourseService courseService) {
        this.examService = examService;
        this.courseService = courseService;
    }

    public List<ExamResponse> findAll() {
        return ExamExtensions.toResponse(examService.findAll());
    }

    public ExamResponse findById(String id) {
        return ExamExtensions.toResponse(examService.findByIdNotNull(id));
    }

    public ExamResponse create(ExamRequest request) {
        Course course = courseService.findByIdNotNull(request.courseId());
        return ExamExtensions.toResponse(examService.create(ExamExtensions.toDto(request, course)));
    }

    public ExamResponse update(String id, ExamRequest request) {
        Course course = courseService.findByIdNotNull(request.courseId());
        return ExamExtensions.toResponse(examService.update(id, ExamExtensions.toDto(request, course)));
    }

    public ExamResponse deleteById(String id) {
        return ExamExtensions.toResponse(examService.deleteById(id));
    }
}
