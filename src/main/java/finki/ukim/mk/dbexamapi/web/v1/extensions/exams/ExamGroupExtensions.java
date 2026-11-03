package finki.ukim.mk.dbexamapi.web.v1.extensions.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.ExamGroupDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;
import finki.ukim.mk.dbexamapi.web.v1.requests.exams.ExamGroupRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.exams.ExamGroupResponse;

import java.util.List;

public final class ExamGroupExtensions {

    private ExamGroupExtensions() {
    }

    public static ExamGroupDto toDto(ExamGroupRequest request, Exam exam) {
        return new ExamGroupDto(
                exam,
                request.name(),
                request.sortOrder()
        );
    }

    public static ExamGroupResponse toResponse(ExamGroup examGroup) {
        return new ExamGroupResponse(
                examGroup.getId(),
                examGroup.getExam().getId(),
                examGroup.getName(),
                examGroup.getSortOrder()
        );
    }

    public static List<ExamGroupResponse> toResponse(List<ExamGroup> examGroups) {
        return examGroups.stream()
                .map(ExamGroupExtensions::toResponse)
                .toList();
    }
}
