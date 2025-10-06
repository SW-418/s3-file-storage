package samwells.io.s3uploader.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import samwells.io.s3uploader.entity.Upload;
import samwells.io.s3uploader.entity.UploadPart;
import samwells.io.s3uploader.exception.MultipartUploadCompletionException;
import samwells.io.s3uploader.exception.MultipartUploadFailedException;
import samwells.io.s3uploader.exception.UploadPartAlreadyCompleteException;
import samwells.io.s3uploader.model.MultipartUpload;
import samwells.io.s3uploader.model.MultipartUploadPart;
import samwells.io.s3uploader.repository.UploadPartRepository;
import samwells.io.s3uploader.repository.UploadRepository;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@AllArgsConstructor
public class ClientManualUploadService implements ClientUploadService {
    private static final String BUCKET_NAME = "s3-uploader-storage";
    private static long PART_SIZE_IN_BYTES = ((5 * 1024) * 1024);

    private final S3AsyncClient s3AsyncClient;
    private final UploadRepository uploadRepository;
    private final UploadPartRepository uploadPartRepository;
    private final S3Presigner s3Presigner;

    @Override
    @Transactional
    public MultipartUpload initiateUpload(String fileName, long fileSizeInBytes) {
        CompletableFuture<CreateMultipartUploadResponse> initiateFuture = createMultipartUpload(fileName);

        CompletableFuture<Upload> uploadEntityFuture = initiateFuture.thenApply(uploadResponse -> {
            String uploadId = uploadResponse.uploadId();

            Upload upload = new Upload();
            upload.setExternalId(uploadId);
            upload.setDirectoryName(BUCKET_NAME);
            upload.setFileName(fileName);
            List<MultipartUploadPart> multipartUploadParts = createUploadParts(uploadId, fileName, fileSizeInBytes);

            // Create UploadPart entity and add to Upload
            multipartUploadParts.forEach(multipartUploadPart -> {
                samwells.io.s3uploader.entity.UploadPart uploadPartEntity = new samwells.io.s3uploader.entity.UploadPart();

                uploadPartEntity.setPartNumber(multipartUploadPart.partNumber());
                uploadPartEntity.setPartSizeInBytes(multipartUploadPart.partSizeInBytes());
                uploadPartEntity.setUrl(multipartUploadPart.url());

                upload.addUploadPart(uploadPartEntity);
            });
            return uploadRepository.save(upload);
        });


        try {
            return uploadEntityFuture.thenCompose(upload -> {
                List<MultipartUploadPart> uploadParts = upload.getUploadParts()
                        .stream()
                        .map(MultipartUploadPart::new)
                        .toList();

                MultipartUpload result = new MultipartUpload(upload.getId(), uploadParts);

                return CompletableFuture.completedFuture(result);
            }).get();
        } catch (Exception exception) {
            log.error("Epic fail", exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    @Transactional
    public void abortUpload(Long id) {
        Upload upload = uploadRepository.getReferenceById(id);
        String uploadId = upload.getExternalId();
        String fileName = upload.getFileName();

        try {
            abortMultipartUpload(uploadId, fileName)
                    .thenCompose(abortResponse -> {
                        log.info("Aborted multipart upload");
                        return CompletableFuture.completedFuture(null);
                    }).get();
        } catch (Exception ex) {
            log.error("Failed to abort multipart upload");
            throw new RuntimeException(ex);
        }

        uploadRepository.delete(upload);
    }

    @Override
    @Transactional
    public MultipartUploadPart completeUploadPart(Long uploadId, Long uploadPartId, String completionTag) {
        // Set etag on part if not already exists
        UploadPart part = uploadPartRepository.getReferenceById(uploadPartId);

        if (part.getCompletionTag() != null) throw new UploadPartAlreadyCompleteException(uploadId, uploadPartId);

        part.setCompletionTag(completionTag);

        uploadPartRepository.save(part);

        // TODO: Publish message
        return new MultipartUploadPart(part);
    }

    @Override
    public void completeUpload(Long uploadId) {
        Upload upload = uploadRepository.getUploadAndPartsByUploadId(uploadId);
        try {
            completeMultipartUpload(upload.getExternalId(), upload.getFileName(), upload.getUploadParts()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MultipartUploadCompletionException("Encountered error completing upload");
        }
    }

    private CompletableFuture<CreateMultipartUploadResponse> createMultipartUpload(String fileName) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest
                .builder()
                .bucket(BUCKET_NAME)
                .key(fileName)
                .build();
        return s3AsyncClient.createMultipartUpload(request);
    }

    private CompletableFuture<AbortMultipartUploadResponse> abortMultipartUpload(String uploadId, String fileName) {
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest
                .builder()
                .key(fileName)
                .uploadId(uploadId)
                .bucket(BUCKET_NAME)
                .build();

        return s3AsyncClient.abortMultipartUpload(request);
    }

    private CompletableFuture<CompleteMultipartUploadResponse> completeMultipartUpload(String uploadId, String fileName, List<UploadPart> uploadParts) {
        List<CompletedPart> completedParts = uploadParts
                .stream()
                .map(part -> {
                    if (part.getCompletionTag() == null || part.getCompletionTag().isEmpty()) {
                        throw new MultipartUploadCompletionException(
                                String.format("Upload part with id %s did not have a completion tag. This is required to complete the upload", part.getId())
                        );
                    }

                    return CompletedPart
                            .builder()
                            .eTag(part.getCompletionTag())
                            .partNumber(part.getPartNumber())
                            .build();
                })
                .toList();

        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest
                .builder()
                .key(fileName)
                .uploadId(uploadId)
                .bucket(BUCKET_NAME)
                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                .build();

        return s3AsyncClient.completeMultipartUpload(request);
    }

    private List<MultipartUploadPart> createUploadParts(String uploadId, String fileName, long fileSizeInBytes) {
        List<MultipartUploadPart> parts = new ArrayList<>();

        try {
            int partNumber = 1;
            for (long i = 0; i < fileSizeInBytes; i += PART_SIZE_IN_BYTES) {
                long partSize = Math.min(PART_SIZE_IN_BYTES, fileSizeInBytes - i);
                UploadPartRequest uploadPartRequest = UploadPartRequest
                        .builder()
                        .bucket(BUCKET_NAME)
                        .uploadId(uploadId)
                        .key(fileName)
                        .partNumber(partNumber)
                        .contentLength(partSize)
                        .build();

                PresignedUploadPartRequest req = s3Presigner.presignUploadPart(
                        UploadPartPresignRequest
                                .builder()
                                // Presigned URLs last for 15 mins
                                .signatureDuration(Duration.of(15, ChronoUnit.MINUTES))
                                .uploadPartRequest(uploadPartRequest)
                                .build()
                );

                parts.add(new MultipartUploadPart(partNumber, req.url().toString(), partSize));
                partNumber++;
            }
        } catch (Exception e) {
            log.error("Encountered exception generating presigned URLs", e);
            throw e;
        }

        return parts;
    }
}
