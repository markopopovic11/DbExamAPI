package finki.ukim.mk.dbexamapi.web.v1.mappers.content;

import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.content.Folder;
import finki.ukim.mk.dbexamapi.service.content.DocumentService;
import finki.ukim.mk.dbexamapi.service.content.FolderService;
import finki.ukim.mk.dbexamapi.web.v1.extensions.content.DocumentExtensions;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.DocumentRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.DocumentFileResponse;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.DocumentResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class DocumentMapper {

    private final DocumentService documentService;
    private final FolderService folderService;

    public DocumentMapper(DocumentService documentService, FolderService folderService) {
        this.documentService = documentService;
        this.folderService = folderService;
    }

    public List<DocumentResponse> findAll() {
        return DocumentExtensions.toResponse(documentService.findAll());
    }

    public DocumentResponse findById(String id) {
        return DocumentExtensions.toResponse(documentService.findByIdNotNull(id));
    }

    public DocumentResponse create(DocumentRequest request) {
        Folder folder = request.folderId() == null ? null : folderService.findByIdNotNull(request.folderId());
        return DocumentExtensions.toResponse(
                documentService.create(DocumentExtensions.toDto(request, folder, null)));
    }

    public DocumentResponse upload(MultipartFile file, String folderId, String name, int sortOrder) {
        Folder folder = folderId == null ? null : folderService.findByIdNotNull(folderId);
        String documentName = name == null || name.isBlank() ? file.getOriginalFilename() : name;
        try (InputStream content = file.getInputStream()) {
            return DocumentExtensions.toResponse(documentService.upload(
                    DocumentExtensions.toUploadDto(folder, documentName, file.getContentType(), file.getSize(), sortOrder),
                    file.getOriginalFilename(),
                    content));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read the uploaded file", exception);
        }
    }

    public DocumentResponse update(String id, DocumentRequest request) {
        Folder folder = request.folderId() == null ? null : folderService.findByIdNotNull(request.folderId());
        Document existing = documentService.findByIdNotNull(id);
        return DocumentExtensions.toResponse(
                documentService.update(id, DocumentExtensions.toDto(request, folder, existing)));
    }

    public DocumentResponse deleteById(String id) {
        return DocumentExtensions.toResponse(documentService.deleteById(id));
    }

    public DocumentFileResponse downloadFile(String id) {
        Document document = documentService.findByIdNotNull(id);
        return new DocumentFileResponse(documentService.loadFile(id), document.getName(), document.getMimeType());
    }
}
