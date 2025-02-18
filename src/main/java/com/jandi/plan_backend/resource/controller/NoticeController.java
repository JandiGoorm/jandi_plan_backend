package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.NoticeListDTO;
import com.jandi.plan_backend.resource.dto.NoticeWritePostDTO;
import com.jandi.plan_backend.resource.dto.NoticeWriteRespDTO;
import com.jandi.plan_backend.resource.service.NoticeService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeService noticeService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 관련


    public NoticeController(NoticeService noticeService, JwtTokenProvider jwtTokenProvider) {
        this.noticeService = noticeService;
        this.jwtTokenProvider = jwtTokenProvider;
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

    /** 공지사항 추가 API */
    @PostMapping("/lists")
    public ResponseEntity<?> writeNotice(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody NoticeWritePostDTO noticeDTO // JSON 형식으로 공지글 작성 정보 받기
    ){

        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 공지글 저장 및 반환
        NoticeWriteRespDTO savedNotice = noticeService.writeNotice(noticeDTO, userEmail);
        return ResponseEntity.ok(savedNotice);
    }


}

