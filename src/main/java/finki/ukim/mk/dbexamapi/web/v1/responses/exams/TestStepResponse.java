package finki.ukim.mk.dbexamapi.web.v1.responses.exams;

public record TestStepResponse(
        String id,
        String taskId,
        int sortOrder,
        String actionSql,
        boolean expectError,
        String checkSql
) {
}
