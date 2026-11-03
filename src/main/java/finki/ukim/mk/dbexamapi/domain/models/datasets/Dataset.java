package finki.ukim.mk.dbexamapi.domain.models.datasets;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseAuditableEntity;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "DATASETS")
public class Dataset extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ddl", columnDefinition = "TEXT", nullable = false)
    private String ddl;
}
