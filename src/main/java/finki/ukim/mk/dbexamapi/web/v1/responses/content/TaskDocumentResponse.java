package finki.ukim.mk.dbexamapi.web.v1.responses.content;

public record TaskDocumentResponse(
        String id,
        String taskId,
        String documentId,
        int sortOrder
) {
}
