package finki.ukim.mk.dbexamapi.web.v1.responses.exams;

public record ExamGroupResponse(
        String id,
        String examId,
        String name,
        int sortOrder
) {
}
