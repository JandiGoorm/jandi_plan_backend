package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.service.CommentService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommentController {
    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    public CommentController(CommentService commentService, JwtTokenProvider jwtTokenProvider) {
        this.commentService = commentService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 댓글 조회 API */
    @GetMapping("/comments/{postId}")
    public Map<String, Object> getComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails

    ){
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        Page<ParentCommentDTO> parentCommentsPage = commentService.getAllComments(postId, page, size, userEmail);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", parentCommentsPage.getNumber(),
                        "currentSize", parentCommentsPage.getContent().size(),
                        "totalPages", parentCommentsPage.getTotalPages(),
                        "totalSize", parentCommentsPage.getTotalElements()
                ),
                "items", parentCommentsPage.getContent()
        );
    }

    /** 답글 조회 API */
    @GetMapping("/replies/{commentId}")
    public Map<String, Object> getReplies(
            @PathVariable Integer commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails

    ){
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        Page<RepliesDTO> repliesPage = commentService.getAllReplies(commentId, page, size, userEmail);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", repliesPage.getNumber(),
                        "currentSize", repliesPage.getContent().size(),
                        "totalPages", repliesPage.getTotalPages(),
                        "totalSize", repliesPage.getTotalElements()
                ),
                "items", repliesPage.getContent()
        );
    }

    /** 댓글 작성 API */
    @PostMapping("/comments/{postId}")
    public ResponseEntity<?> writeComment(
            @PathVariable Integer postId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommentReqDTO commentDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 댓글 저장 및 반환
        CommentRespDTO savedComment = commentService.writeComment(commentDTO, postId, userEmail);
        return ResponseEntity.ok(savedComment);
    }

    /** 답글 작성 API */
    @PostMapping("/replies/{commentId}")
    public ResponseEntity<?> writeReplie(
            @PathVariable Integer commentId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommentReqDTO commentDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 댓글 저장 및 반환
        CommentRespDTO savedComment = commentService.writeReplies(commentDTO, commentId, userEmail);
        return ResponseEntity.ok(savedComment);
    }

    /** 댓글 및 답글 수정 API */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Integer commentId, //경로 변수로 변경할 댓글/답글 아이디 받기
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommentReqDTO commentDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 게시물 수정 및 반환
        CommentRespDTO updatedPost = commentService.updateComment(commentDTO, commentId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }

    /** 댓글 및 답글 삭제 API */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteReplies(
            @PathVariable Integer commentId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 답글 삭제 및 반환
        int deletedRepliesCount = (commentService.deleteComments(commentId, userEmail));
        String returnMsg = (deletedRepliesCount == 0) ?
                "댓글이 삭제되었습니다": "선택된 댓글과 하위 답글 " + deletedRepliesCount +"개가 삭제되었습니다";
        return ResponseEntity.ok(returnMsg);
    }


    /** 댓글 좋아요 API */
    @PostMapping("/comments/likes/{commentId}")
    public ResponseEntity<?> likeComment(
            @PathVariable Integer commentId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        commentService.likeComment(userEmail, commentId);
        return ResponseEntity.ok("좋아요 성공");
    }

    /** 댓글 좋아요 취소 API */
    @DeleteMapping("/comments/likes/{commentId}")
    public ResponseEntity<?> deleteLikeComment(
            @PathVariable Integer commentId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        commentService.deleteLikeComment(userEmail, commentId);
        return ResponseEntity.ok("좋아요 취소되었습니다.");
    }

    /** 댓글 신고 API */
    @PostMapping("/comments/reports/{commentId}")
    public ResponseEntity<?> reportComment(
            @PathVariable Integer commentId,
            @RequestBody ReportReqDTO reportDTO,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        CommentReportRespDTO reported = commentService.reportComment(userEmail, commentId, reportDTO);
        return ResponseEntity.ok(reported);
    }

}
