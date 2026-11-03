package finki.ukim.mk.dbexamapi.domain.models.exams;

import finki.ukim.mk.dbexamapi.domain.enums.TaskDataSource;
import finki.ukim.mk.dbexamapi.domain.enums.TaskType;
import finki.ukim.mk.dbexamapi.domain.models.common.BaseAuditableEntity;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TASKS")
public class Task extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /**
     * Null = in every group of the exam. Must be a group of this task's own exam (service-layer validated).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private ExamGroup group;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TaskType type;

    /**
     * QUERY and CODE only, null for the rest.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source")
    private TaskDataSource dataSource;

    /**
     * OWN_SCHEMA only: the earlier DDL task whose tables it runs on.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_task_id")
    private Task sourceTask;

    /**
     * DATASETS only: a dataset of this task's own exam (service-layer validated).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    /**
     * The professor's SQL. Source of truth, server only, never sent to the PC.
     */
    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "points", nullable = false)
    private int points;

    /**
     * Unique within what one group sees: its own tasks plus the shared ones (service-layer validated).
     */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
