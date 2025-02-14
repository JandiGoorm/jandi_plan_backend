package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.NoticeListDTO;
import com.jandi.plan_backend.resource.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**페이지 단위로 공지글 리스트 조회*/
    @GetMapping("/lists")
    public Map<String, Object> getAllNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NoticeListDTO> noticePage = noticeService.getAllNotices(page, size);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", noticePage.getNumber(),  // 현재 페이지 번호
                        "currentSize", noticePage.getContent().size(), // 현재 페이지 리스트 개수
                        "totalPages", noticePage.getTotalPages(),  // 전체 페이지 개수
                        "totalSize", noticePage.getTotalElements() // 전체 게시물 개수
                ),
                "items", noticePage.getContent()   // 현재 페이지의 게시물 데이터
        );
    }
}

