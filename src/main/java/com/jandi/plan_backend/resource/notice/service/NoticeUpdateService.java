package com.jandi.plan_backend.resource.notice.service;

import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.image.service.InMemoryTempPostService;
import com.jandi.plan_backend.resource.notice.dto.NoticeFinalizeReqDTO;
import com.jandi.plan_backend.resource.notice.dto.NoticeReqDTO;
import com.jandi.plan_backend.resource.notice.dto.NoticeRespDTO;
import com.jandi.plan_backend.resource.notice.entity.Notice;
import com.jandi.plan_backend.resource.notice.repository.NoticeRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TimeUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeUpdateService {

    private final NoticeRepository noticeRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final InMemoryTempPostService inMemoryTempPostService;

    /**
     * 공지사항 최종 작성 API
     * 임시 Notice ID(음수)를 실제 Notice ID로 전환하며,
     * 이미지의 targetId를 임시에서 실제 Notice ID로 업데이트합니다.
     */
    // 스프링시큐리티에서 권한없는 사람을 걸러내므로 별도의 관리자 검증 로직 제거
    @Transactional
    public NoticeRespDTO finalizeNotice(String userEmail, NoticeFinalizeReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);

        // 임시 tempId 검증
        int tempNoticeId = reqDTO.getTempNoticeId();
        inMemoryTempPostService.validateTempId(tempNoticeId, user.getUserId());

        // 공지 생성
        Notice notice = createNoticeData(reqDTO);

        // 이미지의 임시 noticeId를 실제 noticeId로 업데이트
        int realNoticeId = notice.getNoticeId();
        imageService.updateTargetId("notice", tempNoticeId, realNoticeId);
        inMemoryTempPostService.removeTempId(tempNoticeId);

        return new NoticeRespDTO(notice);
    }

    /** 공지사항 수정 */
    // 스프링시큐리티에서 권한없는 사람을 걸러내므로 별도의 관리자 검증 로직 제거
    @Transactional
    public NoticeRespDTO updateNotice(NoticeReqDTO noticeDTO, Integer noticeId) {
        Notice notice = validationUtil.validateNoticeExists(noticeId);

        updateNoticeData(notice, noticeDTO);
        return new NoticeRespDTO(notice);
    }

    /**
     * 공지사항 삭제
     * 공지사항에 연결된 모든 이미지도 함께 삭제
     */
    // 스프링시큐리티에서 권한없는 사람을 걸러내므로 별도의 관리자 검증 로직 제거
    @Transactional
    public boolean deleteNotice(Integer noticeId) {
        // 공지사항 검증
        Notice notice = validationUtil.validateNoticeExists(noticeId);

        deleteNoticeData(notice);
        return !noticeRepository.existsById(noticeId);
    }

    // 공지사항 추가 메서드
    private Notice createNoticeData(NoticeFinalizeReqDTO reqDTO) {
        Notice notice = new Notice();
        notice.setTitle(reqDTO.getTitle());
        notice.setContents(reqDTO.getContent());
        notice.setCreatedAt(TimeUtil.now());
        noticeRepository.save(notice);

        return notice;
    }

    // 공지사항 수정 메서드
    private void updateNoticeData(Notice notice, NoticeReqDTO reqDTO) {
        if (reqDTO.getTitle() != null) {
            notice.setTitle(reqDTO.getTitle());
        }
        if (reqDTO.getContent() != null) {
            notice.setContents(reqDTO.getContent());
        }
    }

    // 공지사항 삭제 메서드
    private void deleteNoticeData(Notice notice) {
        Integer noticeId = notice.getNoticeId();
        noticeRepository.delete(notice);

        // 연결된 모든 이미지 삭제: 트랜잭션 커밋 이후 외부에서 진행
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("notice", noticeId);
        runAfterCommit("deletePost 이미지 삭제", () -> {
            for (Image image : images) {
                try {
                    imageService.deleteImage(image.getImageId());
                } catch (Exception e) {
                    log.warn("이미지 삭제 실패 - ID: {}, 에러: {}", image.getImageId(), e.getMessage());
                }
            }
        });
    }


    // 트랜잭션 작업이 성공적으로 커밋된 이후에 실행될 작업을 지정
    // 이미지 삭제는 외부 클라우드이므로 트랜잭션과 분리하여 이미지 삭제 실패가 불완전한 롤백(이미지는 삭제되었는데 공지사항은 롤백되는 상황)으로 이어지지 않도록 함
    private void runAfterCommit(String methodName, Runnable task) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    task.run();
                } catch (Exception e) {
                    log.warn("{} 작업 중 예외 발생: {}", methodName, e.getMessage());
                }
            }
        });
    }
}