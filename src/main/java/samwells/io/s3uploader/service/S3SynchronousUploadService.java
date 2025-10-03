package samwells.io.s3uploader.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import samwells.io.s3uploader.exception.MultipartUploadFailedException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Qualifier("sync")
@AllArgsConstructor
public class S3SynchronousUploadService implements UploadService {
    private final S3Client s3Client;
    private final String BUCKET_NAME = "s3-uploader-storage";

    @Override
    public void upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String uploadId = initiateMultipartUpload(fileName);
        try {
            List<CompletedPart> completedParts = splitAndUpload(file, uploadId);

            // Mark upload as complete to prevent storage charges
            completeMultipartUpload(uploadId, fileName, completedParts);
        } catch (MultipartUploadFailedException exception) {
            // Abort upload on any failure to prevent storage charges
            abortMultipartUpload(uploadId, fileName);
        }
    }

    private String initiateMultipartUpload(String fileName) {
        try {
            log.info("Initiating multipart upload for file {}", fileName);
            CreateMultipartUploadRequest request = CreateMultipartUploadRequest
                    .builder()
                    .bucket(BUCKET_NAME)
                    .key(fileName)
                    .build();

            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
            String uploadId = response.uploadId();
            log.info("Successfully initiated multipart upload for file {} with uploadId {}", fileName, uploadId);
            return uploadId;
        } catch (Exception exception) {
            log.error("Failed to initiate multipart upload for file {}", fileName, exception);
            throw exception;
        }
    }

    private void completeMultipartUpload(String uploadId, String fileName, List<CompletedPart> completedParts) {
        log.info("Marking multipart upload {} as complete", uploadId);
        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest
                .builder()
                .bucket(BUCKET_NAME)
                .uploadId(uploadId)
                .key(fileName)
                .multipartUpload(
                        CompletedMultipartUpload
                                .builder()
                                .parts(completedParts)
                                .build()
                )
                .build();

        s3Client.completeMultipartUpload(request);
        log.info("Successfully marked multipart upload {} as complete", uploadId);
    }

    private void abortMultipartUpload(String uploadId, String fileName) {
        log.info("Aborting multipart upload {}", uploadId);
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest
                .builder()
                .bucket(BUCKET_NAME)
                .uploadId(uploadId)
                .key(fileName)
                .build();
        s3Client.abortMultipartUpload(request);
        log.info("Successfully aborted multipart upload {}", uploadId);
    }

    private List<CompletedPart> splitAndUpload(MultipartFile file, String uploadId) throws MultipartUploadFailedException {
        List<CompletedPart> completedParts  = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();
        long currentPosition = 0;

        // Split file into 5MB chunks
        int partSizeInBytes = ((5 * 1024) * 1024);
        int partNumber = 1;

        try (InputStream stream = file.getInputStream()) {
            while (currentPosition < fileSize) {
                int size = Math.min(partSizeInBytes, stream.available());
                log.info("{}/{} - {}", currentPosition / 1024, fileSize / 1024, size / 1024);
                byte[] buffer = new byte[size];

                stream.readNBytes(buffer, 0, size);

                UploadPartRequest uploadRequest = UploadPartRequest
                        .builder()
                        .bucket(BUCKET_NAME)
                        .uploadId(uploadId)
                        .contentLength((long) size)
                        .partNumber(partNumber)
                        .key(fileName)
                        .build();

                log.info("Uploading part {}", partNumber);
                // Could probably delegate this to a thread pool and continue;
                UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadRequest, RequestBody.fromBytes(buffer));
                log.info("Successfully uploaded part {}", partNumber);

                // Mark upload of part as complete
                completedParts.add(
                        CompletedPart
                                .builder()
                                .partNumber(partNumber)
                                .eTag(uploadPartResponse.eTag())
                                .build()
                );

                currentPosition += size;
                partNumber++;
            }
        } catch (IOException exception) {
            String errorMessage = "Encountered error reading from file stream";
            log.error(errorMessage, exception);
            throw new MultipartUploadFailedException(errorMessage);
        } catch (SdkException sdkException) {
            String errorMessage = "Encountered exception uploading file part";
            log.error(errorMessage, sdkException);
            throw new MultipartUploadFailedException(errorMessage);
        }

        return completedParts;
    }
}
