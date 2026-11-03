package finki.ukim.mk.dbexamapi.domain.models.datasets;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseAuditableEntity;
import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One set of data for a dataset's tables. Exactly one visible version per dataset (service-layer validated).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "DATASET_VERSIONS", uniqueConstraints = {
        @UniqueConstraint(name = "uq_dataset_versions_dataset_name", columnNames = {"dataset_id", "name"})
})
public class DatasetVersion extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "visible", nullable = false)
    private boolean visible = false;

    /**
     * The .sql file with the INSERTs. Never served to students, only the executable pulls it.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
