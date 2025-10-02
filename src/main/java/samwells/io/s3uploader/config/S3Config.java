package samwells.io.s3uploader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Configuration
public class S3Config {

    @Bean
    // This is using the least performant, synchronous client
    S3Client s3Client() {
        return S3Client
                .builder()
                .region(Region.CA_CENTRAL_1)
                 // Use AWS SSO CLI and custom profile - Sign in via CLI and the SDK handles the rest 😘
                .credentialsProvider(ProfileCredentialsProvider.create("s3-uploader"))
                .build();
    }

    @Bean
    S3AsyncClient s3AsyncClient() {
        return S3AsyncClient
                .builder()
                .multipartEnabled(true)
                .region(Region.CA_CENTRAL_1)
                 // Use AWS SSO CLI and custom profile - Sign in via CLI and the SDK handles the rest 😘
                .credentialsProvider(ProfileCredentialsProvider.create("s3-uploader"))
                .build();
    }

    @Bean
    VirtualThreadTaskExecutor taskExecutor() {
        return new VirtualThreadTaskExecutor("virtual-");
    }

    @Bean
    S3TransferManager transferManager(
            S3AsyncClient s3AsyncClient,
            VirtualThreadTaskExecutor executor
    ) {
        return S3TransferManager
                .builder()
                .s3Client(s3AsyncClient)
                .executor(executor)
                .build();
    }
}
