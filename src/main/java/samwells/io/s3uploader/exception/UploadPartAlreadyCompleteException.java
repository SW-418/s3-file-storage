package samwells.io.s3uploader.exception;

public class UploadPartAlreadyCompleteException extends RuntimeException {
    public UploadPartAlreadyCompleteException(
            Long uploadId,
            Long uploadPartId
    ) {
        super(String.format("Upload part %d for upload %s is already marked as complete", uploadPartId, uploadId));
    }
}
