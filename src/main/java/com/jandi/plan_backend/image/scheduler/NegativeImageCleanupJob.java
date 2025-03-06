package com.jandi.plan_backend.image.scheduler;

import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NegativeImageCleanupJob {

    private final ImageRepository imageRepository;
    private final ImageService imageService;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupNegativeTargetImages() {
        log.info("Starting cleanup of images with negative targetId.");
        // targetId가 음수인 모든 이미지 조회
        List<Image> negativeImages = imageRepository.findAllByTargetIdLessThan(0);
        log.info("Found {} images with negative targetId.", negativeImages.size());
        for (Image image : negativeImages) {
            try {
                imageService.deleteImage(image.getImageId());
                log.info("Deleted image id {} with negative targetId", image.getImageId());
            } catch (Exception e) {
                log.error("Failed to delete image id {}: {}", image.getImageId(), e.getMessage());
            }
        }
        log.info("Cleanup of negative targetId images completed.");
    }
}
