package samwells.io.s3uploader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;

@Service
@Slf4j
public class S3UploadService implements UploadService {
    private final S3Client s3Client;
    private final String BUCKET_NAME = "s3-uploader-storage";

    public S3UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void upload(MultipartFile file) {
        HeadBucketResponse response = s3Client.headBucket(HeadBucketRequest.builder().bucket(BUCKET_NAME).build());
        log.info("{}", response);
    }
}
