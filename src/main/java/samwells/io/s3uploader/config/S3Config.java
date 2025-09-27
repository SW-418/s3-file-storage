package samwells.io.s3uploader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Bean
    S3Client s3Client() {
        // This is using the least performant, synchronous client
        return S3Client
                .builder()
                .region(Region.CA_CENTRAL_1)
                 // Use AWS SSO CLI and custom profile - Sign in via CLI and the SDK handles the rest 😘
                .credentialsProvider(ProfileCredentialsProvider.create("s3-uploader"))
                .build();
    }
}
