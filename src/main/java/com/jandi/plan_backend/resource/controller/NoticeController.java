package com.jandi.plan_backend.resource.controller;

import com.jandi.plan_backend.resource.dto.NoticeReqDTO;
import com.jandi.plan_backend.resource.dto.NoticeRespDTO;
import com.jandi.plan_backend.resource.dto.NoticeFinalizeReqDTO;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.service.NoticeService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /** 공지글 전체 목록 조회 API */
    @GetMapping("/lists")
    public Map<String, Object> getAllNotices() {
        List<Notice> noticeList = noticeService.getAllNotices();
        return Map.of(
                "items", noticeList   // 현재 페이지의 게시물 데이터
        );
    }

    /**
     * 공지사항 최종 작성 API
     * 임시 Notice ID(음수)를 실제 Notice ID로 전환하며, 이미지의 targetId를 업데이트합니다.
     */
    @PostMapping("/")
    public ResponseEntity<?> finalizeNotice(
            @RequestHeader("Authorization") String token,
            @RequestBody NoticeFinalizeReqDTO finalizeReqDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        NoticeRespDTO finalizedNotice = noticeService.finalizeNotice(userEmail, finalizeReqDTO);
        return ResponseEntity.ok(finalizedNotice);
    }

    /** 공지사항 수정 API */
    @PatchMapping("/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Integer noticeId, // 링크에서 noticeId 받기
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody NoticeReqDTO noticeDTO // JSON 형식으로 공지글 작성 정보 받기
    ){
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        NoticeRespDTO savedNotice = noticeService.updateNotice(noticeDTO, noticeId, userEmail);
        return ResponseEntity.ok(savedNotice);
    }

    /** 공지사항 삭제 API */
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable Integer noticeId, // 삭제할 공지사항 ID
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        String returnMsg = (noticeService.deleteNotice(userEmail, noticeId)) ?
                "삭제되었습니다" : "삭제 과정에서 문제가 발생했습니다. 다시 한번 시도해주세요";
        return ResponseEntity.ok(returnMsg);
    }
}
