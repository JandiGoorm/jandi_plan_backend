package com.jandi.plan_backend.resource.notice.service;

import com.jandi.plan_backend.resource.notice.dto.NoticeListDTO;
import com.jandi.plan_backend.resource.notice.entity.Notice;
import com.jandi.plan_backend.resource.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService {
    private final NoticeRepository noticeRepository;

    /** 공지사항 목록 조회 */
    @Transactional(readOnly = true)
    public List<NoticeListDTO> getAllNotices() {
        List<Notice> notices = noticeRepository.findAll();
        return notices.stream()
                .map(NoticeListDTO::new)
                .collect(Collectors.toList());
    }
}
