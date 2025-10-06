package samwells.io.s3uploader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import samwells.io.s3uploader.entity.Upload;

public interface UploadRepository extends JpaRepository<Upload, Long> {
    @Query("""
            SELECT u
            FROM Upload u
            JOIN FETCH u.uploadParts
            WHERE u.id = :id
    """)
    Upload getUploadAndPartsByUploadId(@Param("id") Long id);
}
