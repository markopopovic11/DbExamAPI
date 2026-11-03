package finki.ukim.mk.dbexamapi.web.v1.responses.content;

public record ExamFolderResponse(
        String id,
        String examId,
        String folderId,
        int sortOrder
) {
}
