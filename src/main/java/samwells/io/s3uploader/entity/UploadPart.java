package samwells.io.s3uploader.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "upload_part")
public class UploadPart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "upload_id", nullable = false)
    Upload upload;

    @Column(name = "url", nullable = false)
    String url;

    @Column(name = "part_number", nullable = false)
    int partNumber;

    @Column(name = "completion_tag")
    String completionTag;

    @Column(name = "part_size_in_bytes", nullable = false)
    Long partSizeInBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    Long version;
}
