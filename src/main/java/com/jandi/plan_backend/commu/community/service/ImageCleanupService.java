package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCleanupService {
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    @Value("${image-prefix}") private String prefix;

    /**
     * 게시글 내용에서 실제 사용 중인 이미지 파일명을 추출합니다.
     * 예: "https://storage.googleapis.com/plan-storage/encodedFileName.jpg"에서 "encodedFileName.jpg" 추출
     */
    public Set<String> extractImageFileNamesFromContent(String content) {
        Set<String> fileNames = new HashSet<>();
        Pattern pattern = Pattern.compile(prefix + "([^\"\\s]+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            fileNames.add(matcher.group(1));
        }
        return fileNames;
    }

    /**
     * 게시글과 연결된 이미지 중, 게시글 내용에 포함되지 않은 이미지를 삭제합니다.
     */
    // 게시글에 연결된 이미지 삭제에 실패하더라도 일단 게시글 작업은 유지되어야 하므로 이미지 작업과 관련된 물리 트랜잭션을 별도 분리함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupUnusedImages(Community post) {
        Set<String> usedFileNames = extractImageFileNamesFromContent(post.getContents());
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("community", post.getPostId());
        for (Image image : images) {
            if (!usedFileNames.contains(image.getImageUrl())) {
                try {
                    imageService.deleteImage(image.getImageId());
                } catch (Exception e) {
                    log.warn("이미지 정리 실패 - 이미지 ID: {}, 에러: {}", image.getImageId(), e.getMessage());
                }
            }
        }
    }
}
