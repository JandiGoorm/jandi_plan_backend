package com.jandi.plan_backend.image.service;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ImageService {

    private final LocalImageStorageService storageService;
    private final ImageRepository imageRepository;
    private final String urlPrefix;

    public ImageService(LocalImageStorageService storageService,
                        ImageRepository imageRepository,
                        @Value("${image-prefix}") String urlPrefix) {
        this.storageService = storageService;
        this.imageRepository = imageRepository;
        this.urlPrefix = urlPrefix;
    }

    /**
     * 기존 이미지를 지우기 전에 업로드할 파일을 검증합니다.
     */
    public void validateUpload(MultipartFile file) {
        storageService.validateUpload(file);
    }

    /**
     * DB에 저장된 파일명으로 공개 URL을 만듭니다.
     */
    public String toPublicUrl(String storedFileName) {
        return urlPrefix + storedFileName;
    }

    /**
     * 파일을 업로드한 후, 이미지 정보를 DB에 저장하고 공개 URL을 반환합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param owner 업로더 이메일
     * @param targetId 대상 엔티티의 식별자
     * @param targetType 이미지가 속하는 대상
     * @return 업로드 결과를 담은 ImageRespDto
     */
    public ImageRespDto uploadImage(MultipartFile file, String owner, Integer targetId, String targetType) {
        String uploadResult = storageService.uploadFile(file);
        String storedFileName = uploadResult.replace("파일 업로드 성공: ", "").trim();
        Image image = new Image();
        image.setTargetType(targetType);
        image.setTargetId(targetId);
        image.setImageUrl(storedFileName);
        image.setOwner(owner);
        image.setCreatedAt(TimeUtil.now());
        image = imageRepository.save(image);
        String fullPublicUrl = toPublicUrl(image.getImageUrl());
        ImageRespDto responseDto = new ImageRespDto();
        responseDto.setImageId(image.getImageId());
        responseDto.setImageUrl(fullPublicUrl);
        responseDto.setMessage("이미지 업로드 및 DB 저장 성공");
        return responseDto;
    }

    public void updateTargetId(String targetType, int oldTargetId, int newTargetId) {
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId(targetType, oldTargetId);
        for (Image image : images) {
            image.setTargetId(newTargetId);
            imageRepository.save(image);
        }
    }

    /**
     * 이미지 ID를 기반으로 공개 URL을 반환합니다.
     *
     * @param imageId 조회할 이미지의 ID
     * @return 전체 공개 URL 또는 이미지가 없으면 null
     */
    public String getPublicUrlByImageId(Integer imageId) {
        Optional<Image> imageOptional = imageRepository.findById(imageId);
        return imageOptional.map(img -> toPublicUrl(img.getImageUrl())).orElse(null);
    }

    /**
     * targetType과 targetId를 이용해 이미지를 조회하는 메서드.
     *
     * @param targetType 이미지가 속하는 대상 (예: "profile", "community", 등)
     * @param targetId 대상 엔티티의 식별자
     * @return 해당 조건에 맞는 Image 엔티티 (Optional)
     */
    public Optional<Image> getImageByTarget(String targetType, Integer targetId) {
        return imageRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    /**
     * 이미지 업데이트 기능.
     * 기존 이미지(이미지 ID 기준)를 찾아, 기존 파일을 저장소에서 삭제한 후,
     * 새 파일을 업로드하여 DB 레코드를 업데이트합니다.
     *
     * @param imageId 업데이트할 이미지의 DB ID
     * @param newFile 새로 업로드할 이미지 파일
     * @return 업데이트된 이미지 정보를 담은 ImageRespDto, 이미지가 없으면 null 반환
     */
    public ImageRespDto updateImage(Integer imageId, MultipartFile newFile) {
        Optional<Image> optionalImage = imageRepository.findById(imageId);
        if (optionalImage.isEmpty()) {
            log.warn("업데이트할 이미지가 존재하지 않습니다. 이미지 ID: {}", imageId);
            return null;
        }
        Image image = optionalImage.get();
        validateUpload(newFile);
        // 새 파일을 먼저 저장해 저장 실패 시 기존 파일을 유지
        String uploadResult = storageService.uploadFile(newFile);
        String newStoredFileName = uploadResult.replace("파일 업로드 성공: ", "").trim();
        boolean storageDeleted = storageService.deleteFile(image.getImageUrl());
        if (!storageDeleted) {
            log.warn("기존 파일 삭제 실패. 이미지 ID: {}", imageId);
        }
        image.setImageUrl(newStoredFileName);
        image.setCreatedAt(TimeUtil.now());
        image = imageRepository.save(image);
        String fullPublicUrl = toPublicUrl(image.getImageUrl());
        ImageRespDto responseDto = new ImageRespDto();
        responseDto.setImageId(image.getImageId());
        responseDto.setImageUrl(fullPublicUrl);
        responseDto.setMessage("이미지 업데이트 및 DB 저장 성공");
        return responseDto;
    }

    /**
     * 삭제 시, DB의 imageUrl = rawFileName
     * storageService.deleteFile(rawFileName) → 로컬 파일 삭제
     */
    public boolean deleteImage(Integer imageId) {
        Optional<Image> imageOptional = imageRepository.findById(imageId);
        if (imageOptional.isEmpty()) {
            log.warn("삭제할 이미지가 DB에 존재하지 않습니다. 이미지 ID: {}", imageId);
            return false;
        }
        Image image = imageOptional.get();

        // DB에 인코딩된 형태로 저장되어 있어도,
        // storageService.deleteFile(...) 내부에서 URLDecoder.decode(...)
        // -> 저장된 파일명으로 삭제
        boolean storageDeleted = storageService.deleteFile(image.getImageUrl());
        if (storageDeleted) {
            imageRepository.delete(image);
            log.info("이미지 삭제 완료: 이미지 ID: {}", imageId);
            return true;
        } else {
            log.warn("저장소에서 이미지 삭제 실패: 이미지 ID: {}", imageId);
            return false;
        }
    }

}