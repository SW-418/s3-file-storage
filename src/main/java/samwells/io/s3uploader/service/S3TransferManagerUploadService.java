package samwells.io.s3uploader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@Qualifier("managed")
public class S3TransferManagerUploadService implements UploadService {
    private final S3TransferManager transferManager;
    private final String BUCKET_NAME = "s3-uploader-storage";

    public S3TransferManagerUploadService(S3TransferManager transferManager) {
        this.transferManager = transferManager;
    }

    @Override
    public void upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        File convertedFile = toFile(file);

        try {
            UploadFileRequest uploadFileRequest = UploadFileRequest
                    .builder()
                    .source(convertedFile)
                    .addTransferListener(LoggingTransferListener.create())
                    .putObjectRequest(
                            PutObjectRequest
                                    .builder()
                                    .key(fileName)
                                    .bucket(BUCKET_NAME)
                                    .build()
                    )
                    .build();

            transferManager.uploadFile(uploadFileRequest);
        } catch (Exception e) {
            log.error("Encountered error uploading file to S3", e);
            throw new RuntimeException(e);
        }
    }

    private File toFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            File tempFile = File.createTempFile("tmp-", fileName);
            file.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            log.error("Encountered error converting MultipartFile to File", e);
            throw new RuntimeException(e);
        }
    }
}
