package finki.ukim.mk.dbexamapi.repository.exams;

import finki.ukim.mk.dbexamapi.domain.models.exams.ExamGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamGroupRepository extends JpaRepository<ExamGroup, String> {

    boolean existsByExam_IdAndName(String examId, String name);

    boolean existsByExam_IdAndNameAndIdNot(String examId, String name, String id);
}
