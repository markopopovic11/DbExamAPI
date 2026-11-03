package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.ExamGroupDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.ExamGroupDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.ExamGroupExamImmutableException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.ExamGroupInUseException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.ExamGroupNameAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;
import finki.ukim.mk.dbexamapi.repository.exams.ExamGroupRepository;
import finki.ukim.mk.dbexamapi.service.exams.ExamGroupService;
import finki.ukim.mk.dbexamapi.service.exams.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ExamGroupServiceImpl implements ExamGroupService {

    private final ExamGroupRepository examGroupRepository;
    private final TaskService taskService;

    public ExamGroupServiceImpl(ExamGroupRepository examGroupRepository, TaskService taskService) {
        this.examGroupRepository = examGroupRepository;
        this.taskService = taskService;
    }

    @Override
    public Optional<ExamGroup> findById(String id) {
        return examGroupRepository.findById(id);
    }

    @Override
    public ExamGroup findByIdNotNull(String id) {
        return examGroupRepository.findById(id)
                .orElseThrow(() -> new ExamGroupDoesNotExistException(id));
    }

    @Override
    public List<ExamGroup> findAll() {
        return examGroupRepository.findAll();
    }

    @Override
    @Transactional
    public ExamGroup create(ExamGroupDto examGroupDto) {
        String examId = examGroupDto.exam().getId();
        if (examGroupRepository.existsByExam_IdAndName(examId, examGroupDto.name())) {
            throw new ExamGroupNameAlreadyExistsException(examId, examGroupDto.name());
        }

        ExamGroup examGroup = new ExamGroup();
        examGroup.setExam(examGroupDto.exam());
        examGroup.setName(examGroupDto.name());
        examGroup.setSortOrder(examGroupDto.sortOrder());

        ExamGroup saved = examGroupRepository.save(examGroup);
        log.info("Created exam group with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExamGroup update(String id, ExamGroupDto examGroupDto) {
        ExamGroup examGroup = findByIdNotNull(id);

        String examId = examGroup.getExam().getId();
        if (!examGroupDto.exam().getId().equals(examId)) {
            throw new ExamGroupExamImmutableException(id);
        }
        if (examGroupRepository.existsByExam_IdAndNameAndIdNot(examId, examGroupDto.name(), id)) {
            throw new ExamGroupNameAlreadyExistsException(examId, examGroupDto.name());
        }

        examGroup.setName(examGroupDto.name());
        examGroup.setSortOrder(examGroupDto.sortOrder());

        ExamGroup saved = examGroupRepository.save(examGroup);
        log.info("Updated exam group with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ExamGroup deleteById(String id) {
        ExamGroup examGroup = findByIdNotNull(id);
        if (taskService.existsByGroupId(id)) {
            throw new ExamGroupInUseException(id);
        }
        examGroupRepository.delete(examGroup);
        log.info("Deleted exam group with id: {}", id);
        return examGroup;
    }
}
