package samwells.io.s3uploader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import samwells.io.s3uploader.entity.UploadPart;

@Repository
public interface UploadPartRepository extends JpaRepository<UploadPart, Long> { }
