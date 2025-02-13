package com.jandi.plan_backend.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class GcpCredentialsConfig {

    @Value("${gcp.credentials.key.base64}")
    private String gcpSaKeyBase64;

    @Bean
    public CredentialsProvider googleCredentialsProvider() throws IOException {
        // Base64 디코딩 후 GoogleCredentials 생성
        byte[] decodedBytes = Base64.getDecoder().decode(gcpSaKeyBase64);
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes));
        return FixedCredentialsProvider.create(credentials);
    }

    @Bean
    public Storage googleStorage(CredentialsProvider credentialsProvider) {
        try {
            StorageOptions storageOptions = StorageOptions.newBuilder()
                    .setCredentials(credentialsProvider.getCredentials())
                    .build();
            return storageOptions.getService();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create GCP Storage", e);
        }
    }
}
