package finki.ukim.mk.dbexamapi.web.v1.mappers.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.service.exams.ExamGroupService;
import finki.ukim.mk.dbexamapi.service.exams.ExamService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.exams.ExamGroupExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.ExamGroupRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.ExamGroupResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamGroupMapper {

    private final ExamGroupService examGroupService;
    private final ExamService examService;

    public ExamGroupMapper(ExamGroupService examGroupService,
                           ExamService examService) {
        this.examGroupService = examGroupService;
        this.examService = examService;
    }

    public List<ExamGroupResponse> findAll() {
        return ExamGroupExtensions.toResponse(examGroupService.findAll());
    }

    public ExamGroupResponse findById(String id) {
        return ExamGroupExtensions.toResponse(examGroupService.findByIdNotNull(id));
    }

    public ExamGroupResponse create(ExamGroupRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        return ExamGroupExtensions.toResponse(examGroupService.create(ExamGroupExtensions.toDto(request, exam)));
    }

    public ExamGroupResponse update(String id, ExamGroupRequest request) {
        Exam exam = examService.findByIdNotNull(request.examId());
        return ExamGroupExtensions.toResponse(examGroupService.update(id, ExamGroupExtensions.toDto(request, exam)));
    }

    public ExamGroupResponse deleteById(String id) {
        return ExamGroupExtensions.toResponse(examGroupService.deleteById(id));
    }
}
