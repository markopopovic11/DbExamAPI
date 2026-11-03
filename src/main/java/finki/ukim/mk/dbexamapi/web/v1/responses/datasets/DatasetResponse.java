package finki.ukim.mk.dbexamapi.web.v1.responses.datasets;

public record DatasetResponse(
        String id,
        String examId,
        String name,
        String ddl
) {
}
