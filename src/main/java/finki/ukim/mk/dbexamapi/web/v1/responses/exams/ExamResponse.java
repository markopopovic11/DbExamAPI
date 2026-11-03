package finki.ukim.mk.dbexamapi.web.v1.responses.exams;

import java.time.Instant;

public record ExamResponse(
        String id,
        String courseId,
        String name,
        Instant startsAt,
        Instant endsAt
) {
}
