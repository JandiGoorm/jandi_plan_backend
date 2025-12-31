package com.jandi.plan_backend.commu.comment.controller;

import com.jandi.plan_backend.commu.comment.dto.CommentReportRespDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentReqDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentRespDTO;
import com.jandi.plan_backend.commu.comment.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.comment.service.CommentLikeService;
import com.jandi.plan_backend.commu.comment.service.CommentReportService;
import com.jandi.plan_backend.commu.comment.service.CommentUpdateService;
import com.jandi.plan_backend.commu.community.dto.*;
import com.jandi.plan_backend.commu.comment.service.CommentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommentController {
    private final CommentLikeService commentLikeService;
    private final CommentQueryService commentQueryService;
    private final CommentReportService commentReportService;
    private final CommentUpdateService commentUpdateService;

    /** 댓글 조회 API */
    @GetMapping("/comments/{postId}")
    public Map<String, Object> getComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        Page<ParentCommentDTO> parentCommentsPage = commentQueryService.getAllComments(postId, page, size, userEmail);

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
        Page<RepliesDTO> repliesPage = commentQueryService.getAllReplies(commentId, page, size, userEmail);

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
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentReqDTO commentDTO
    ){
        String userEmail = userDetails.getUsername();
        CommentRespDTO savedComment = commentUpdateService.writeComment(commentDTO, postId, userEmail);
        return ResponseEntity.ok(savedComment);
    }

    /** 답글 작성 API */
    @PostMapping("/replies/{commentId}")
    public ResponseEntity<?> writeReplie(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentReqDTO commentDTO
    ){
        String userEmail = userDetails.getUsername();
        CommentRespDTO savedComment = commentUpdateService.writeReplies(commentDTO, commentId, userEmail);
        return ResponseEntity.ok(savedComment);
    }

    /** 댓글 및 답글 수정 API */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentReqDTO commentDTO
    ){
        String userEmail = userDetails.getUsername();
        CommentRespDTO updatedPost = commentUpdateService.updateComment(commentDTO, commentId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }

    /** 댓글 및 답글 삭제 API */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteReplies(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String userEmail = userDetails.getUsername();
        int deletedRepliesCount = commentUpdateService.deleteComments(commentId, userEmail);
        String returnMsg = (deletedRepliesCount == 0) ?
                "댓글이 삭제되었습니다": "선택된 댓글과 하위 답글 " + deletedRepliesCount +"개가 삭제되었습니다";
        return ResponseEntity.ok(returnMsg);
    }

    /** 댓글 좋아요 API */
    @PostMapping("/comments/likes/{commentId}")
    public ResponseEntity<?> likeComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String userEmail = userDetails.getUsername();
        commentLikeService.likeComment(userEmail, commentId);
        return ResponseEntity.ok("좋아요 성공");
    }

    /** 댓글 좋아요 취소 API */
    @DeleteMapping("/comments/likes/{commentId}")
    public ResponseEntity<?> deleteLikeComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String userEmail = userDetails.getUsername();
        commentLikeService.deleteLikeComment(userEmail, commentId);
        return ResponseEntity.ok("좋아요 취소되었습니다.");
    }

    /** 댓글 신고 API */
    @PostMapping("/comments/reports/{commentId}")
    public ResponseEntity<?> reportComment(
            @PathVariable Integer commentId,
            @RequestBody ReportReqDTO reportDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String userEmail = userDetails.getUsername();
        CommentReportRespDTO reported = commentReportService.reportComment(userEmail, commentId, reportDTO);
        return ResponseEntity.ok(reported);
    }
}
