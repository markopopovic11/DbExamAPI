package finki.ukim.mk.dbexamapi.domain.models.datasets;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseEntity;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import finki.ukim.mk.dbexamapi.domain.models.exams.TestStep;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Hash of the solution's result on a version. QUERY: one row per version, step null.
 * CODE: one row per version and test step. Since step is null for QUERY, the service also
 * checks one row per (task, version).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "EXPECTED_RESULTS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_expected_results_task_version_step", columnNames = {"task_id", "version_id", "step_id"})
})
public class ExpectedResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private DatasetVersion version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private TestStep step;

    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;
}
