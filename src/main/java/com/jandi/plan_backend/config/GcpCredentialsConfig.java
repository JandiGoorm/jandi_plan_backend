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

/**
 * GCP의 서비스 계정 자격 증명을 이용해서 Storage 객체를 생성하는 설정 클래스.
 * gcp.credentials.key.base64 프로퍼티에 Base64로 인코딩된 서비스 계정 JSON 문자열이 저장되어 있어.
 */
@Configuration
public class GcpCredentialsConfig {

    // application.properties나 환경변수에 저장된 Base64 인코딩된 서비스 계정 JSON 문자열
    @Value("${gcp.credentials.key.base64}")
    private String gcpSaKeyBase64;

    /**
     * GCP CredentialsProvider 빈을 생성하는 메서드.
     * 1. Base64로 인코딩된 서비스 계정 키를 디코딩.
     * 2. 디코딩된 바이트 배열을 ByteArrayInputStream으로 감싸서 GoogleCredentials 객체를 생성.
     * 3. FixedCredentialsProvider로 감싼 후 반환.
     */
    @Bean
    public CredentialsProvider googleCredentialsProvider() throws IOException {
        // gcpSaKeyBase64에 저장된 문자열을 Base64 디코더를 이용해서 바이트 배열로 변환
        byte[] decodedBytes = Base64.getDecoder().decode(gcpSaKeyBase64);
        // 바이트 배열을 InputStream으로 감싸서 GoogleCredentials 객체 생성
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decodedBytes));
        // FixedCredentialsProvider를 통해 CredentialsProvider 빈으로 등록
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     * GCP Storage 빈을 생성하는 메서드.
     * googleCredentialsProvider()에서 생성한 CredentialsProvider를 주입받아 StorageOptions에 설정.
     * StorageOptions를 통해 Storage 객체를 생성하고 반환.
     */
    @Bean
    public Storage googleStorage(CredentialsProvider credentialsProvider) {
        try {
            StorageOptions storageOptions = StorageOptions.newBuilder()
                    // CredentialsProvider에서 자격 증명을 가져와서 설정
                    .setCredentials(credentialsProvider.getCredentials())
                    .build();
            // StorageOptions를 이용해 Storage 객체 생성 후 반환
            return storageOptions.getService();
        } catch (IOException e) {
            // 자격 증명을 가져오는 중 문제가 생기면 IllegalStateException 발생시킴
            throw new IllegalStateException("GCP Storage 생성 실패", e);
        }
    }
}
