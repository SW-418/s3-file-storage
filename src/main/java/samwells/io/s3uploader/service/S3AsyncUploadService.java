package samwells.io.s3uploader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import samwells.io.s3uploader.exception.MultipartUploadFailedException;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Slf4j
@Service
@Qualifier("async")
public class S3AsyncUploadService implements UploadService {
    private final S3AsyncClient s3Client;
    private final String BUCKET_NAME = "s3-uploader-storage";

    public S3AsyncUploadService(S3AsyncClient s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void upload(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        CompletableFuture<String> uploadIdFuture = initiateMultipartUpload(fileName);

        CompletableFuture<Void> uploadFuture = uploadIdFuture.thenCompose(uploadId -> {
            List<CompletableFuture<CompletedPart>> completableFutures = splitAndUpload(file, uploadId);
            // Await and join all futures at this point
            return CompletableFuture
                    .allOf(completableFutures.toArray(new CompletableFuture[0]))
                    .thenCompose(completedParts ->
                            completeMultipartUpload(uploadId, fileName, completableFutures.stream().map(CompletableFuture::join).toList())
                                    .thenCompose(e -> CompletableFuture.<Void>completedFuture(null))
                    )
                    .handle((response, exception) -> {
                        if (exception != null) {
                            return abortMultipartUpload(uploadId, fileName)
                                    .thenCompose(r -> CompletableFuture.<Void>failedFuture(exception));
                        }

                        return CompletableFuture.<Void>completedFuture(null);
                    }).thenCompose(Function.identity());
        });

        try {
            uploadFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Encountered exception uploading file", e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<AbortMultipartUploadResponse> abortMultipartUpload(String uploadId, String fileName) {
        log.info("Aborting multipart upload {}", uploadId);
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest
                .builder()
                .bucket(BUCKET_NAME)
                .uploadId(uploadId)
                .key(fileName)
                .build();
        return s3Client.abortMultipartUpload(request)
                .handle((response, exception) -> {
                    if (exception != null) {
                        log.error("Failed to abort multipart upload {}",uploadId);
                        throw new CompletionException(exception);
                    }

                    log.info("Successfully aborted multipart upload {}", uploadId);
                    return response;
                });
    }

    private CompletableFuture<CompleteMultipartUploadResponse> completeMultipartUpload(String uploadId, String fileName, List<CompletedPart> completedParts) {
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

        return s3Client.completeMultipartUpload(request)
                .handle((response, exception) -> {
                    if (exception != null) {
                        log.error("Failed to complete multipart upload {}", uploadId);
                        throw new CompletionException(exception);
                    }

                    log.info("Successfully marked multipart upload {} as complete", uploadId);
                    return response;
                });
    }

    private CompletableFuture<String> initiateMultipartUpload(String fileName) {
        log.info("Initiating multipart upload for file {}", fileName);
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest
                .builder()
                .bucket(BUCKET_NAME)
                .key(fileName)
                .build();

        return s3Client
                .createMultipartUpload(request)
                .thenApply(response -> {
                    log.info("Successfully initiated multipart upload for file {} with uploadId {}", fileName, response.uploadId());
                    return response.uploadId();
                })
                .exceptionally(exception -> {
                    log.error("Failed to initiate multipart upload for file {}", fileName, exception);
                    throw new CompletionException("Failed to initiate multipart upload", exception);
                });
    }

    private List<CompletableFuture<CompletedPart>> splitAndUpload(MultipartFile file, String uploadId) {
        List<CompletableFuture<CompletedPart>> completedParts = new ArrayList<>();
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
                int currentPart = partNumber;
                CompletableFuture<CompletedPart> completedPart = s3Client.uploadPart(uploadRequest, AsyncRequestBody.fromBytes(buffer))
                        .handle((response, exception) -> {
                            if (exception != null) {
                                String errorMessage = "Encountered exception uploading file part";
                                log.error(errorMessage, exception);
                                throw new CompletionException(new MultipartUploadFailedException(errorMessage));
                            }

                            log.info("Successfully uploaded part {}", currentPart);
                            return CompletedPart
                                    .builder()
                                    .partNumber(currentPart)
                                    .eTag(response.eTag())
                                    .build();
                        });

                completedParts.add(completedPart);

                currentPosition += size;
                partNumber++;
            }
        } catch (IOException exception) {
            String errorMessage = "Encountered error reading from file stream";
            log.error(errorMessage, exception);
            throw new CompletionException(new MultipartUploadFailedException(errorMessage));
        }

        return completedParts;
    }
}
