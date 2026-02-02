package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lot_documents")
@Data
@NoArgsConstructor
public class LotDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Lot association
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "lot_id",
            referencedColumnName = "lot_identifier",
            nullable = false
    )
    private Lot lot;

    // Uploader association
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uploader_user_id",
            referencedColumnName = "user_id",
            nullable = false
    )
    private Users uploader;

    @Column(name = "uploader_name", nullable = false)
    private String uploaderName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "is_image", nullable = false)
    private Boolean isImage;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    public LotDocument(@NonNull Lot lot,
                       @NonNull Users uploader,
                       @NonNull String uploaderName,
                       @NonNull String originalFileName,
                       @NonNull String storageKey,
                       @NonNull String mimeType,
                       @NonNull Long sizeBytes,
                       @NonNull Boolean isImage) {
        this.lot = lot;
        this.uploader = uploader;
        this.uploaderName = uploaderName;
        this.originalFileName = originalFileName;
        this.storageKey = storageKey;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.isImage = isImage;
    }
}
