package samwells.io.s3uploader.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "upload")
public class Upload {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    String externalId;

    @Column(name = "directory_name", nullable = false)
    String directoryName;

    @Column(name = "file_name", nullable = false)
    String fileName;

    @OneToMany(mappedBy = "upload", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<UploadPart> uploadParts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    Long version;

    public void addUploadPart(UploadPart uploadPart) {
        // TODO: Make this thread-safe
        uploadParts.add(uploadPart);
        uploadPart.setUpload(this);
    }
}
