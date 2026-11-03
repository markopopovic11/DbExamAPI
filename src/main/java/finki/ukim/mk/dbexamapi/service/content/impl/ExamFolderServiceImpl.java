package finki.ukim.mk.dbexamapi.service.content.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.content.ExamFolderDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.ExamFolderAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.ExamFolderDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.content.ExamFolder;
import finki.ukim.mk.dbexamapi.repository.content.ExamFolderRepository;
import finki.ukim.mk.dbexamapi.service.content.ExamFolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ExamFolderServiceImpl implements ExamFolderService {

    private final ExamFolderRepository examFolderRepository;

    public ExamFolderServiceImpl(ExamFolderRepository examFolderRepository) {
        this.examFolderRepository = examFolderRepository;
    }

    @Override
    public Optional<ExamFolder> findById(String id) {
        return examFolderRepository.findById(id);
    }

    @Override
    public ExamFolder findByIdNotNull(String id) {
        return examFolderRepository.findById(id)
                .orElseThrow(() -> new ExamFolderDoesNotExistException(id));
    }

    @Override
    public List<ExamFolder> findAll() {
        return examFolderRepository.findAll();
    }

    @Override
    @Transactional
    public ExamFolder create(ExamFolderDto examFolderDto) {
        String examId = examFolderDto.exam().getId();
        String folderId = examFolderDto.folder().getId();
        if (examFolderRepository.existsByExam_IdAndFolder_Id(examId, folderId)) {
            throw new ExamFolderAlreadyExistsException(examId, folderId);
        }

        ExamFolder examFolder = new ExamFolder();
        examFolder.setExam(examFolderDto.exam());
        examFolder.setFolder(examFolderDto.folder());
        examFolder.setSortOrder(examFolderDto.sortOrder());

        ExamFolder saved = examFolderRepository.save(examFolder);
        log.info("Created exam folder with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExamFolder update(String id, ExamFolderDto examFolderDto) {
        ExamFolder examFolder = findByIdNotNull(id);

        String examId = examFolderDto.exam().getId();
        String folderId = examFolderDto.folder().getId();
        if (examFolderRepository.existsByExam_IdAndFolder_IdAndIdNot(examId, folderId, id)) {
            throw new ExamFolderAlreadyExistsException(examId, folderId);
        }

        examFolder.setExam(examFolderDto.exam());
        examFolder.setFolder(examFolderDto.folder());
        examFolder.setSortOrder(examFolderDto.sortOrder());

        ExamFolder saved = examFolderRepository.save(examFolder);
        log.info("Updated exam folder with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExamFolder deleteById(String id) {
        ExamFolder examFolder = findByIdNotNull(id);
        examFolderRepository.delete(examFolder);
        log.info("Deleted exam folder with id: {}", id);
        return examFolder;
    }
}
