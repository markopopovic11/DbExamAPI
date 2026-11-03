package finki.ukim.mk.dbexamapi.web.v1.responses.exams;

public record CourseResponse(
        String id,
        String code,
        String name,
        boolean active
) {
}
