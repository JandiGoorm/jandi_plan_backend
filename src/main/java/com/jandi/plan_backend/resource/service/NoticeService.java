package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.dto.NoticeListDTO;
import com.jandi.plan_backend.resource.dto.NoticeWritePostDTO;
import com.jandi.plan_backend.resource.dto.NoticeWriteRespDTO;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.repository.NoticeRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;


    public NoticeService(NoticeRepository noticeRepository, UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
    }

    /** 공지글 목록 조회*/
    public Page<NoticeListDTO> getAllNotices(int page, int size) {
        long totalCount = noticeRepository.count();
        return PaginationService.getPagedData(page, size, totalCount, noticeRepository::findAll, NoticeListDTO::new);
    }

    /** 공지글 작성 */
    public NoticeWriteRespDTO writeNotice(NoticeWritePostDTO noticeDTO, String email) {
        // 유저 검증
        User user = validateUserExists(email);
        validateUserIsAdmin(user);

        // 공지글 생성
        Notice notice = new Notice();
        notice.setCreatedAt(LocalDateTime.now());
        notice.setTitle(noticeDTO.getTitle());
        notice.setContents(noticeDTO.getContents());

        // DB 저장 및 반환
        noticeRepository.save(notice);
        return new NoticeWriteRespDTO(notice);
    }

    // 사용자의 존재 여부 검증
    private User validateUserExists(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    // 유저가 관리자인지 검증
    private void validateUserIsAdmin(User user) {
        if(user.getUserId() != 1)
            throw new BadRequestExceptionMessage("공지사항을 작성할 권한이 없습니다");
    }


}
