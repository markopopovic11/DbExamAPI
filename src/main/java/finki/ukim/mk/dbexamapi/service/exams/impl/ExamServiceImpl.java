package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.ExamDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.ExamDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.repository.exams.ExamRepository;
import finki.ukim.mk.dbexamapi.service.exams.ExamService;
import finki.ukim.mk.dbexamapi.helpers.exams.ExamRuleHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamRuleHelper examRuleHelper;

    public ExamServiceImpl(ExamRepository examRepository, ExamRuleHelper examRuleHelper) {
        this.examRepository = examRepository;
        this.examRuleHelper = examRuleHelper;
    }

    @Override
    public Optional<Exam> findById(String id) {
        return examRepository.findById(id);
    }

    @Override
    public Exam findByIdNotNull(String id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ExamDoesNotExistException(id));
    }

    @Override
    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    @Override
    @Transactional
    public Exam create(ExamDto examDto) {
        examRuleHelper.requireCourseActive(examDto.course());
        examRuleHelper.requireEndsAfterStart(examDto);

        Exam exam = new Exam();
        exam.setCourse(examDto.course());
        exam.setName(examDto.name());
        exam.setStartsAt(examDto.startsAt());
        exam.setEndsAt(examDto.endsAt());

        Exam saved = examRepository.save(exam);
        log.info("Created exam with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Exam update(String id, ExamDto examDto) {
        Exam exam = findByIdNotNull(id);

        if (!examDto.course().getId().equals(exam.getCourse().getId())) {
            examRuleHelper.requireCourseActive(examDto.course());
        }
        examRuleHelper.requireEndsAfterStart(examDto);

        exam.setCourse(examDto.course());
        exam.setName(examDto.name());
        exam.setStartsAt(examDto.startsAt());
        exam.setEndsAt(examDto.endsAt());

        Exam saved = examRepository.save(exam);
        log.info("Updated exam with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Exam deleteById(String id) {
        Exam exam = findByIdNotNull(id);
        examRepository.delete(exam);
        log.info("Deleted exam with id: {}", id);
        return exam;
    }
}
