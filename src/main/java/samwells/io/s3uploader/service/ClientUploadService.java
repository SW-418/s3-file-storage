package samwells.io.s3uploader.service;

import samwells.io.s3uploader.model.MultipartUpload;

public interface ClientUploadService {
    MultipartUpload initiateUpload(String fileName, long fileSizeInBytes);
}
