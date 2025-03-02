package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.resource.dto.NoticeFinalizeReqDTO;
import com.jandi.plan_backend.resource.dto.NoticeReqDTO;
import com.jandi.plan_backend.resource.dto.NoticeRespDTO;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.repository.NoticeRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TempPostIdGenerator;
import com.jandi.plan_backend.util.service.InMemoryTempPostService;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final InMemoryTempPostService inMemoryTempPostService;

    public NoticeService(NoticeRepository noticeRepository,
                         ValidationUtil validationUtil,
                         ImageService imageService,
                         ImageRepository imageRepository,
                         InMemoryTempPostService inMemoryTempPostService) {
        this.noticeRepository = noticeRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.inMemoryTempPostService = inMemoryTempPostService;
    }

    /**
     * 공지사항 목록 조회
     */
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    /**
     * 공지사항 작성 (임시 ID 없이 바로 저장)
     */
    public NoticeRespDTO writeNotice(NoticeReqDTO noticeDTO, String userEmail) {
        // 관리자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        // 공지사항 생성
        Notice notice = new Notice();
        notice.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        notice.setTitle(noticeDTO.getTitle());
        notice.setContents(noticeDTO.getContents());

        // DB 저장 및 반환
        noticeRepository.save(notice);
        return new NoticeRespDTO(notice);
    }

    /**
     * 공지사항 수정
     */
    public NoticeRespDTO updateNotice(NoticeReqDTO noticeDTO, Integer noticeId, String userEmail) {
        // 관리자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        // 공지사항 검증
        Notice notice = validationUtil.validateNoticeExists(noticeId);

        // 값이 존재하는 경우에만 수정
        if (noticeDTO.getTitle() != null) {
            notice.setTitle(noticeDTO.getTitle());
        }
        if (noticeDTO.getContents() != null) {
            notice.setContents(noticeDTO.getContents());
        }

        // 수정 후 저장
        noticeRepository.save(notice);
        return new NoticeRespDTO(notice);
    }

    /**
     * 공지사항 삭제
     * 공지사항에 연결된 모든 이미지도 함께 삭제
     */
    public boolean deleteNotice(String userEmail, Integer noticeId) {
        // 관리자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        // 공지사항 검증
        Notice notice = validationUtil.validateNoticeExists(noticeId);

        // 연결된 모든 이미지 삭제
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("notice", noticeId);
        for (Image img : images) {
            imageService.deleteImage(img.getImageId());
        }

        // 공지사항 삭제
        noticeRepository.delete(notice);
        return true;
    }

    /**
     * 공지사항 최종 작성 API
     * 임시 Notice ID(음수)를 실제 Notice ID로 전환하며,
     * 이미지의 targetId를 임시에서 실제 notice ID로 업데이트합니다.
     */
    public NoticeRespDTO finalizeNotice(String userEmail, NoticeFinalizeReqDTO finalizeReqDTO) {
        // 관리자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        // 임시 Notice ID 검증
        int tempNoticeId = finalizeReqDTO.getTempNoticeId();
        inMemoryTempPostService.validateTempId(tempNoticeId, user.getUserId());

        // 실제 Notice 생성
        Notice notice = new Notice();
        notice.setTitle(finalizeReqDTO.getTitle());
        notice.setContents(finalizeReqDTO.getContents());
        notice.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        noticeRepository.save(notice);
        int realNoticeId = notice.getNoticeId();

        // 이미지의 targetId 업데이트 (임시 Notice ID → 실제 Notice ID)
        imageService.updateTargetId("notice", tempNoticeId, realNoticeId);

        // 임시 Notice ID 제거
        inMemoryTempPostService.removeTempId(tempNoticeId);

        return new NoticeRespDTO(notice);
    }
}
