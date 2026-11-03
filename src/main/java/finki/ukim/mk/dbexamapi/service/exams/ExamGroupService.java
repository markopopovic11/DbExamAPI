package finki.ukim.mk.dbexamapi.service.exams;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.ExamGroupDto;
import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;

import java.util.List;
import java.util.Optional;

public interface ExamGroupService {

    Optional<ExamGroup> findById(String id);

    ExamGroup findByIdNotNull(String id);

    List<ExamGroup> findAll();

    ExamGroup create(ExamGroupDto examGroupDto);

    ExamGroup update(String id, ExamGroupDto examGroupDto);

    ExamGroup deleteById(String id);
}
