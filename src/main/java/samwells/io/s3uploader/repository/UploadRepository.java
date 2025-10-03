package samwells.io.s3uploader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import samwells.io.s3uploader.entity.Upload;

public interface UploadRepository extends JpaRepository<Upload, Long> { }
