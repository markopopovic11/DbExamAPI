package finki.ukim.mk.dbexamapi.service.content.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.content.DocumentDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentInUseException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentSourceInvalidException;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.repository.content.DocumentRepository;
import finki.ukim.mk.dbexamapi.service.content.DocumentService;
import finki.ukim.mk.dbexamapi.service.content.TaskDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TaskDocumentService taskDocumentService;

    public DocumentServiceImpl(DocumentRepository documentRepository, TaskDocumentService taskDocumentService) {
        this.documentRepository = documentRepository;
        this.taskDocumentService = taskDocumentService;
    }

    @Override
    public Optional<Document> findById(String id) {
        return documentRepository.findById(id);
    }

    @Override
    public Document findByIdNotNull(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentDoesNotExistException(id));
    }

    @Override
    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    @Override
    @Transactional
    public Document create(DocumentDto documentDto) {
        validateSource(documentDto);

        Document document = new Document();
        document.setFolder(documentDto.folder());
        document.setName(documentDto.name());
        document.setContentMd(documentDto.contentMd());
        document.setFilePath(documentDto.filePath());
        document.setUrl(documentDto.url());
        document.setMimeType(documentDto.mimeType());
        document.setSizeBytes(documentDto.sizeBytes());
        document.setSortOrder(documentDto.sortOrder());

        Document saved = documentRepository.save(document);
        log.info("Created document with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Document update(String id, DocumentDto documentDto) {
        Document document = findByIdNotNull(id);

        validateSource(documentDto);

        document.setFolder(documentDto.folder());
        document.setName(documentDto.name());
        document.setContentMd(documentDto.contentMd());
        document.setFilePath(documentDto.filePath());
        document.setUrl(documentDto.url());
        document.setMimeType(documentDto.mimeType());
        document.setSizeBytes(documentDto.sizeBytes());
        document.setSortOrder(documentDto.sortOrder());

        Document saved = documentRepository.save(document);
        log.info("Updated document with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Document deleteById(String id) {
        Document document = findByIdNotNull(id);
        if (taskDocumentService.existsByDocumentId(id)) {
            throw new DocumentInUseException(id);
        }
        documentRepository.delete(document);
        log.info("Deleted document with id: {}", id);
        return document;
    }

    private void validateSource(DocumentDto documentDto) {
        long sources = Stream.of(documentDto.contentMd(), documentDto.filePath(), documentDto.url())
                .filter(source -> source != null)
                .count();
        if (sources != 1) {
            throw new DocumentSourceInvalidException();
        }
    }
}
