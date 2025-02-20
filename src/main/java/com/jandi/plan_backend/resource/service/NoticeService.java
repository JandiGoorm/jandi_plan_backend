package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.dto.NoticeReqDTO;
import com.jandi.plan_backend.resource.dto.NoticeRespDTO;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.repository.NoticeRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final ValidationUtil validationUtil;

    public NoticeService(NoticeRepository noticeRepository, ValidationUtil validationUtil) {
        this.noticeRepository = noticeRepository;
        this.validationUtil = validationUtil;
    }

    /**
     * 공지글 목록 조회
     */
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    /** 공지글 작성 */
    public NoticeRespDTO writeNotice(NoticeReqDTO noticeDTO, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        // 공지글 생성
        Notice notice = new Notice();
        notice.setCreatedAt(LocalDateTime.now());
        notice.setTitle(noticeDTO.getTitle());
        notice.setContents(noticeDTO.getContents());

        // DB 저장 및 반환
        noticeRepository.save(notice);
        return new NoticeRespDTO(notice);
    }

    public NoticeRespDTO updateNotice(NoticeReqDTO noticeDTO, Integer noticeId, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        // 공지글 검증
        Notice notice = validationUtil.validateNoticeExists(noticeId);

        // 공지 수정: 값이 있는 것만 수정
        if(noticeDTO.getTitle() != null) { notice.setTitle(noticeDTO.getTitle()); }
        if(noticeDTO.getContents() != null) { notice.setContents(noticeDTO.getContents()); }

        //수정된 공지 반환
        noticeRepository.save(notice);
        return new NoticeRespDTO(notice);
    }
}
