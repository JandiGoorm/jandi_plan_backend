package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.CommentReportedListDTO;
import com.jandi.plan_backend.commu.dto.CommunityReportedListDTO;
import com.jandi.plan_backend.commu.service.ManageCommunityService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manage/community")
public class ManageCommunityController {
    private final ManageCommunityService manageCommunityService;

    public ManageCommunityController(ManageCommunityService manageCommunityService) {
        this.manageCommunityService = manageCommunityService;
    }


    /** reported 조회 */
    // 신고 게시물 조회
    @GetMapping("/reported/posts")
    public ResponseEntity<Map<String, Object>> getReportedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        String userEmail = userDetails.getUsername();

        Page<CommunityReportedListDTO> reportedPosts = manageCommunityService.getReportedPosts(userEmail, page, size);

        Map<String, Object> response = Map.of(
                "pageInfo", Map.of(
                        "currentPage", reportedPosts.getNumber(),
                        "currentSize", reportedPosts.getContent().size(),
                        "totalPages", reportedPosts.getTotalPages(),
                        "totalSize", reportedPosts.getTotalElements()
                ),
                "items", reportedPosts.getContent()
        );

        return ResponseEntity.ok(response);  // Map을 ResponseEntity로 감싸서 반환
    }

    // 신고 댓글 조회
    @GetMapping("/reported/comments")
    public ResponseEntity<Map<String, Object>> getReportedComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        String userEmail = userDetails.getUsername();

        Page<CommentReportedListDTO> reportedComments = manageCommunityService.getReportedComments(userEmail, page, size);

        Map<String, Object> response = Map.of(
                "pageInfo", Map.of(
                        "currentPage", reportedComments.getNumber(),
                        "currentSize", reportedComments.getContent().size(),
                        "totalPages", reportedComments.getTotalPages(),
                        "totalSize", reportedComments.getTotalElements()
                ),
                "items", reportedComments.getContent()
        );

        return ResponseEntity.ok(response);  // Map을 ResponseEntity로 감싸서 반환
    }

    // 부적절 게시글 삭제
    @DeleteMapping("/delete/posts/{postId}")
    public ResponseEntity<?> deletePosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer postId
    ){
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try{
            manageCommunityService.deletePosts(postId);
            return ResponseEntity.ok("삭제되었습니다.");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 부적절 게시글 삭제
    @DeleteMapping("/delete/comments/{commentId}")
    public ResponseEntity<?> deleteComments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer commentId
    ){
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try{
            manageCommunityService.deleteComments(commentId);
            return ResponseEntity.ok("삭제되었습니다.");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
