package finki.ukim.mk.dbexamapi.service.content.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.content.DocumentDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentFileDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentInUseException;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.repository.content.DocumentRepository;
import finki.ukim.mk.dbexamapi.service.content.DocumentService;
import finki.ukim.mk.dbexamapi.service.content.FileStorageService;
import finki.ukim.mk.dbexamapi.service.content.TaskDocumentService;
import finki.ukim.mk.dbexamapi.helpers.content.DocumentRuleHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final TaskDocumentService taskDocumentService;
    private final FileStorageService fileStorageService;
    private final DocumentRuleHelper documentRuleHelper;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               TaskDocumentService taskDocumentService,
                               FileStorageService fileStorageService,
                               DocumentRuleHelper documentRuleHelper) {
        this.documentRepository = documentRepository;
        this.taskDocumentService = taskDocumentService;
        this.fileStorageService = fileStorageService;
        this.documentRuleHelper = documentRuleHelper;
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
        documentRuleHelper.requireExactlyOneSource(documentDto);

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
    public Document upload(DocumentDto documentDto, String originalFilename, InputStream content) {
        documentRuleHelper.requireFileNotEmpty(documentDto);

        String filePath = fileStorageService.store(
                originalFilename, documentDto.mimeType(), documentDto.sizeBytes(), content);
        try {
            return create(new DocumentDto(
                    documentDto.folder(),
                    documentDto.name(),
                    null,
                    filePath,
                    null,
                    documentDto.mimeType(),
                    documentDto.sizeBytes(),
                    documentDto.sortOrder()
            ));
        } catch (RuntimeException exception) {
            fileStorageService.delete(filePath);
            throw exception;
        }
    }

    @Override
    @Transactional
    public Document update(String id, DocumentDto documentDto) {
        Document document = findByIdNotNull(id);

        documentRuleHelper.requireExactlyOneSource(documentDto);

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
        if (document.getFilePath() != null) {
            fileStorageService.delete(document.getFilePath());
        }
        log.info("Deleted document with id: {}", id);
        return document;
    }

    @Override
    public Resource loadFile(String id) {
        Document document = findByIdNotNull(id);
        if (document.getFilePath() == null) {
            throw new DocumentFileDoesNotExistException(id);
        }
        return fileStorageService.load(document.getFilePath());
    }
}
