package finki.ukim.mk.dbexamapi.domain.models.content;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseEntity;
import finki.ukim.mk.dbexamapi.domain.models.exams.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TASK_DOCUMENTS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_task_documents_task_document", columnNames = {"task_id", "document_id"})
})
public class TaskDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
