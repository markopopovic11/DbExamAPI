package finki.ukim.mk.dbexamapi.service.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.DocumentDto;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface DocumentService {

    Optional<Document> findById(String id);

    Document findByIdNotNull(String id);

    List<Document> findAll();

    Document create(DocumentDto documentDto);

    /**
     * Stores the file and creates a file document. The dto's content fields are ignored;
     * folder, name, mime type, size and sort order are taken from it.
     */
    Document upload(DocumentDto documentDto, String originalFilename, InputStream content);

    Document update(String id, DocumentDto documentDto);

    /**
     * Also deletes the stored file.
     */
    Document deleteById(String id);

    Resource loadFile(String id);
}
