package finki.ukim.mk.dbexamapi.web.v1.controllers.content;

import finki.ukim.mk.dbexamapi.web.v1.mappers.content.DocumentMapper;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.DocumentRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.DocumentFileResponse;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.DocumentResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/v1/documents")
public class DocumentController {

    private final DocumentMapper documentMapper;

    public DocumentController(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> findAll() {
        return ResponseEntity.ok(documentMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(documentMapper.findById(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        DocumentFileResponse file = documentMapper.downloadFile(id);
        MediaType mediaType = file.mimeType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.mimeType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.name(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(file.file());
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentMapper.create(request));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestPart("file") MultipartFile file,
                                                   @RequestParam(required = false) String folderId,
                                                   @RequestParam(required = false) String name,
                                                   @RequestParam(defaultValue = "0") int sortOrder) {
        return ResponseEntity.ok(documentMapper.upload(file, folderId, name, sortOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentMapper.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DocumentResponse> deleteById(@PathVariable String id) {
        return ResponseEntity.ok(documentMapper.deleteById(id));
    }
}
