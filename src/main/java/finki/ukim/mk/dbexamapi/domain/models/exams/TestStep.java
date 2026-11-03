package finki.ukim.mk.dbexamapi.domain.models.exams;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One action and the check after it, for a CODE task. All steps of a run share one transaction
 * on one version, in order, and are rolled back at the end.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TEST_STEPS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_test_steps_task_sort_order", columnNames = {"task_id", "sort_order"})
})
public class TestStep extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "action_sql", columnDefinition = "TEXT", nullable = false)
    private String actionSql;

    /**
     * True = the action must fail. Only that an error was raised is checked, never its text.
     */
    @Column(name = "expect_error", nullable = false)
    private boolean expectError = false;

    /**
     * Null when expectError and there is nothing to check.
     */
    @Column(name = "check_sql", columnDefinition = "TEXT")
    private String checkSql;
}
