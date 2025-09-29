package samwells.io.s3uploader.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

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
                .region(Region.CA_CENTRAL_1)
                 // Use AWS SSO CLI and custom profile - Sign in via CLI and the SDK handles the rest 😘
                .credentialsProvider(ProfileCredentialsProvider.create("s3-uploader"))
                .build();
    }
}
