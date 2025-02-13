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
import java.nio.charset.StandardCharsets;

/**
 * 이 설정 클래스는 properties에 저장된 평문 JSON (서비스 계정 키)을 사용하여
 * GoogleCredentials를 생성하고, 이를 기반으로 GCP Storage 관련 빈들을 구성합니다.
 *
 * GitHub 시크릿에 'GCP_SA_KEY'라는 이름으로 서비스 계정 JSON 평문이 저장되어 있다고 가정합니다.
 * application.properties에서는 아래와 같이 사용합니다:
 *
 *   gcp.credentials.key=${GCP_SA_KEY}
 *   gcs.bucket.name=your-bucket-name
 */
@Configuration
public class GcpCredentialsConfig {

    // properties 또는 환경 변수에 저장된 평문 JSON 키
    @Value("${gcp.credentials.key}")
    private String gcpSaKey;

    // 사용하고자 하는 GCS 버킷 이름 (필요에 따라 properties에 추가)
    @Value("${gcs.bucket.name}")
    private String bucketName;

    /**
     * 평문 JSON 문자열을 읽어 GoogleCredentials를 생성한 후,
     * FixedCredentialsProvider로 감싸서 반환합니다.
     * IOException은 내부에서 try-catch로 처리하여 런타임 예외로 변환합니다.
     */
    @Bean
    public CredentialsProvider googleCredentialsProvider() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(gcpSaKey.getBytes(StandardCharsets.UTF_8))
            );
            return FixedCredentialsProvider.create(credentials);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load GCP credentials from provided key", e);
        }
    }

    /**
     * 위에서 생성한 CredentialsProvider를 사용하여 GCP Storage 클라이언트를 생성합니다.
     * getCredentials() 호출 시 IOException이 발생할 수 있으므로, 이를 try-catch로 처리합니다.
     */
    @Bean
    public Storage googleStorage(CredentialsProvider credentialsProvider) {
        try {
            StorageOptions storageOptions = StorageOptions.newBuilder()
                    .setCredentials(credentialsProvider.getCredentials())
                    .build();
            return storageOptions.getService();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to obtain GCP Storage service", e);
        }
    }
}
