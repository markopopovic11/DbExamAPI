package finki.ukim.mk.dbexamapi.domain.models.datasets;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseEntity;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A version that tests a task. Must come from the task's own dataset (service-layer validated).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TASK_VERSIONS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_task_versions_task_version", columnNames = {"task_id", "version_id"})
})
public class TaskVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private DatasetVersion version;
}
