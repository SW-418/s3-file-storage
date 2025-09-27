package samwells.io.s3uploader.exception;

public class MultipartUploadFailedException extends Exception {
    public MultipartUploadFailedException(String message) {
        super(message);
    }
}
