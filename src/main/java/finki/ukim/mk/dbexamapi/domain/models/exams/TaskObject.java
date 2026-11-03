package finki.ukim.mk.dbexamapi.domain.models.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskObjectKind;
import finki.ukim.mk.dbexamapi.domain.models.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An object the task asks the student to create. The name matches a captured object to its task,
 * unique within a group of the exam (service-layer validated). QUERY: exactly one VIEW. CODE: one or more.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TASK_OBJECTS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_task_objects_task_name", columnNames = {"task_id", "name"})
})
public class TaskObject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private TaskObjectKind kind;

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Functions and procedures only.
     */
    @Column(name = "arguments")
    private String arguments;

    /**
     * Triggers only.
     */
    @Column(name = "on_table")
    private String onTable;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
