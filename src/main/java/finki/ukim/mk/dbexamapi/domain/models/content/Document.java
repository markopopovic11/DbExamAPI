package finki.ukim.mk.dbexamapi.domain.models.content;

import finki.ukim.mk.dbexamapi.domain.models.common.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Markdown written in the app, an uploaded file or a link.
 * Exactly one of contentMd, filePath and url is set (service-layer validated).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "DOCUMENTS")
public class Document extends BaseAuditableEntity {

    /**
     * Null = not in a folder, like a document made for one task.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "content_md", columnDefinition = "TEXT")
    private String contentMd;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "url")
    private String url;

    /**
     * Null for links.
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * Files only.
     */
    @Column(name = "size_bytes")
    private Integer sizeBytes;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
