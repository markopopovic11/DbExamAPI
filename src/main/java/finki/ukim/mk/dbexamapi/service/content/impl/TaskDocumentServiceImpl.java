package finki.ukim.mk.dbexamapi.service.content.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.content.TaskDocumentDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.TaskDocumentAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.TaskDocumentDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.content.TaskDocument;
import finki.ukim.mk.dbexamapi.repository.content.TaskDocumentRepository;
import finki.ukim.mk.dbexamapi.service.content.TaskDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskDocumentServiceImpl implements TaskDocumentService {

    private final TaskDocumentRepository taskDocumentRepository;

    public TaskDocumentServiceImpl(TaskDocumentRepository taskDocumentRepository) {
        this.taskDocumentRepository = taskDocumentRepository;
    }

    @Override
    public Optional<TaskDocument> findById(String id) {
        return taskDocumentRepository.findById(id);
    }

    @Override
    public TaskDocument findByIdNotNull(String id) {
        return taskDocumentRepository.findById(id)
                .orElseThrow(() -> new TaskDocumentDoesNotExistException(id));
    }

    @Override
    public List<TaskDocument> findAll() {
        return taskDocumentRepository.findAll();
    }

    @Override
    @Transactional
    public TaskDocument create(TaskDocumentDto taskDocumentDto) {
        String taskId = taskDocumentDto.task().getId();
        String documentId = taskDocumentDto.document().getId();
        if (taskDocumentRepository.existsByTask_IdAndDocument_Id(taskId, documentId)) {
            throw new TaskDocumentAlreadyExistsException(taskId, documentId);
        }

        TaskDocument taskDocument = new TaskDocument();
        taskDocument.setTask(taskDocumentDto.task());
        taskDocument.setDocument(taskDocumentDto.document());
        taskDocument.setSortOrder(taskDocumentDto.sortOrder());

        TaskDocument saved = taskDocumentRepository.save(taskDocument);
        log.info("Created task document with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TaskDocument update(String id, TaskDocumentDto taskDocumentDto) {
        TaskDocument taskDocument = findByIdNotNull(id);

        String taskId = taskDocumentDto.task().getId();
        String documentId = taskDocumentDto.document().getId();
        if (taskDocumentRepository.existsByTask_IdAndDocument_IdAndIdNot(taskId, documentId, id)) {
            throw new TaskDocumentAlreadyExistsException(taskId, documentId);
        }

        taskDocument.setTask(taskDocumentDto.task());
        taskDocument.setDocument(taskDocumentDto.document());
        taskDocument.setSortOrder(taskDocumentDto.sortOrder());

        TaskDocument saved = taskDocumentRepository.save(taskDocument);
        log.info("Updated task document with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TaskDocument deleteById(String id) {
        TaskDocument taskDocument = findByIdNotNull(id);
        taskDocumentRepository.delete(taskDocument);
        log.info("Deleted task document with id: {}", id);
        return taskDocument;
    }

    @Override
    public boolean existsByDocumentId(String documentId) {
        return taskDocumentRepository.existsByDocument_Id(documentId);
    }

    @Override
    @Transactional
    public void deleteAllByTaskId(String taskId) {
        taskDocumentRepository.deleteAllByTask_Id(taskId);
        log.info("Deleted task documents of task with id: {}", taskId);
    }
}
