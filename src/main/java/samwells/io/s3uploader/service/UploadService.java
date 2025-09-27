package samwells.io.s3uploader.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    void upload(MultipartFile file);
}
