package finki.ukim.mk.dbexamapi.web.v1.extensions.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.DocumentDto;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.content.Folder;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.DocumentRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.DocumentResponse;

import java.util.List;

public final class DocumentExtensions {

    private static final String MARKDOWN_MIME_TYPE = "text/markdown";

    private DocumentExtensions() {
    }

    /**
     * existing is the document being updated, or null on create. A file document keeps its file:
     * the request cannot set or change a file path.
     */
    public static DocumentDto toDto(DocumentRequest request, Folder folder, Document existing) {
        boolean keepsFile = existing != null && existing.getFilePath() != null;
        return new DocumentDto(
                folder,
                request.name(),
                request.contentMd(),
                keepsFile ? existing.getFilePath() : null,
                request.url(),
                keepsFile ? existing.getMimeType() : request.contentMd() != null ? MARKDOWN_MIME_TYPE : null,
                keepsFile ? existing.getSizeBytes() : null,
                request.sortOrder()
        );
    }

    public static DocumentDto toUploadDto(Folder folder, String name, String mimeType, long sizeBytes, int sortOrder) {
        return new DocumentDto(
                folder,
                name,
                null,
                null,
                null,
                mimeType,
                Math.toIntExact(sizeBytes),
                sortOrder
        );
    }

    public static DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFolder() == null ? null : document.getFolder().getId(),
                document.getName(),
                document.getContentMd(),
                document.getUrl(),
                document.getFilePath() != null,
                document.getMimeType(),
                document.getSizeBytes(),
                document.getSortOrder(),
                document.getUpdatedAt()
        );
    }

    public static List<DocumentResponse> toResponse(List<Document> documents) {
        return documents.stream()
                .map(DocumentExtensions::toResponse)
                .toList();
    }
}
