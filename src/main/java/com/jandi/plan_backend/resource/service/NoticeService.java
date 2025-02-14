package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.dto.NoticeListDTO;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public Page<NoticeListDTO> getAllNotices(int page, int size) {
        System.out.println("getAllNotices");

        //페이지 범위 오류 처리
        long totalCount = noticeRepository.count();
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        if(page < 0 || page > totalPages) {
            throw new RuntimeException("잘못된 페이지 번호 요청");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        return noticePage.map(NoticeListDTO::new);
    }
}
