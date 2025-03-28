package com.jandi.plan_backend.image.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageServiceTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private GoogleCloudStorageService googleCloudStorageService;

    @BeforeEach
    void setUp() {
        // ✅ @Value로 주입되는 필드 수동 설정
        ReflectionTestUtils.setField(googleCloudStorageService, "bucketName", "plan-storage");
    }

    @Test
    void testUploadFile_success() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "dummy content".getBytes()
        );
        Blob blobMock = mock(Blob.class);

        // storage.create 호출되면 blobMock 반환
        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(blobMock);

        // when
        String result = googleCloudStorageService.uploadFile(file);

        // then
        assertTrue(result.startsWith("파일 업로드 성공: "));
        verify(storage, times(1)).create(any(BlobInfo.class), any(byte[].class));
    }
}
