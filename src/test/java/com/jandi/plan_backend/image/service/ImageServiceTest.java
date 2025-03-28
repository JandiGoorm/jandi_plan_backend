package com.jandi.plan_backend.image.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;

class ImageServiceTest {

    @Mock
    private GoogleCloudStorageService googleCloudStorageService;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUploadImage_success() throws Exception {
        // given: 업로드할 파일 생성 (MockMultipartFile 사용)
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "dummy image content".getBytes());
        String owner = "test@example.com";
        Integer targetId = 100;
        String targetType = "community";

        // googleCloudStorageService.uploadFile가 성공적인 메시지를 반환하도록 모킹
        when(googleCloudStorageService.uploadFile(any(MultipartFile.class)))
                .thenReturn("파일 업로드 성공: encodedTest.png");

        // imageRepository.save()가 저장 후 Image 엔티티를 반환하도록 모킹
        Image savedImage = new Image();
        savedImage.setImageId(1);
        savedImage.setTargetType(targetType);
        savedImage.setTargetId(targetId);
        savedImage.setImageUrl("encodedTest.png");
        savedImage.setOwner(owner);
        savedImage.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        // when: 업로드 메서드 호출
        ImageRespDto response = imageService.uploadImage(file, owner, targetId, targetType);

        // then: 응답 DTO의 값 검증
        assertEquals(1, response.getImageId());
        assertEquals(urlPrefix + "encodedTest.png", response.getImageUrl());
        assertEquals("이미지 업로드 및 DB 저장 성공", response.getMessage());

        // googleCloudStorageService.uploadFile과 imageRepository.save가 각각 1번씩 호출되었는지 검증
        verify(googleCloudStorageService, times(1)).uploadFile(any(MultipartFile.class));
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    void testUploadImage_failure() throws Exception {
        // given: 파일 업로드 실패 시, googleCloudStorageService.uploadFile가 실패 메시지를 반환하는 경우
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "dummy image content".getBytes());
        String owner = "test@example.com";
        Integer targetId = 100;
        String targetType = "community";

        when(googleCloudStorageService.uploadFile(any(MultipartFile.class)))
                .thenReturn("파일 업로드 실패: 네트워크 오류");

        // when: 업로드 메서드 호출
        ImageRespDto response = imageService.uploadImage(file, owner, targetId, targetType);

        // then: 실패 메시지가 반환되어야 함
        assertEquals("파일 업로드 실패: 네트워크 오류", response.getMessage());
        // imageRepository.save()는 호출되지 않아야 함
        verify(imageRepository, never()).save(any(Image.class));
    }
}
