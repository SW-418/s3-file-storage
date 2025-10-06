package samwells.io.s3uploader.service;

import samwells.io.s3uploader.model.MultipartUpload;
import samwells.io.s3uploader.model.MultipartUploadPart;

public interface ClientUploadService {
    MultipartUpload initiateUpload(String fileName, long fileSizeInBytes);
    void abortUpload(Long id);
    MultipartUploadPart completeUploadPart(Long uploadId, Long uploadPartId, String completionTag);
}
