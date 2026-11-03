package finki.ukim.mk.dbexamapi.domain.models.content;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseAuditableEntity;
import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "FOLDERS")
public class Folder extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "name", nullable = false)
    private String name;
}
