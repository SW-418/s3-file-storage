package samwells.io.s3uploader.exception;

public class MultipartUploadCompletionException extends RuntimeException {
    public MultipartUploadCompletionException(String message) {
        super(message);
    }
}
