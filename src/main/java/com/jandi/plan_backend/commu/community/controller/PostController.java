package com.jandi.plan_backend.commu.community.controller;

import com.jandi.plan_backend.commu.community.dto.*;
import com.jandi.plan_backend.commu.community.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class PostController {

    private final CommunityLikeService communityLikeService;
    private final CommunityQueryService communityQueryService;
    private final CommunityReportService communityReportService;
    private final CommunitySearchService communitySearchService;
    private final CommunityUpdateService communityUpdateService;

    /** 게시물 목록 조회 API */
    @GetMapping("/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CommunityListDTO> postsPage = communityQueryService.getAllPosts(page, size);
        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", postsPage.getNumber(),
                        "currentSize", postsPage.getContent().size(),
                        "totalPages", postsPage.getTotalPages(),
                        "totalSize", postsPage.getTotalElements()
                ),
                "items", postsPage.getContent()
        );
    }

    /** 특정 게시물 조회 API */
    @GetMapping("/posts/{postId}")
    public Map<String, Object> getPosts(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        return Map.of("items", communityQueryService.getSpecPost(postId, userEmail));
    }

    /** 게시물 수정 API */
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CommunityReqDTO postDTO
    ) {
        String userEmail = userDetails.getUsername();
        CommunityRespDTO updatedPost = communityUpdateService.updatePost(postDTO, postId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }

    /** 게시물 삭제 API - 연결된 모든 이미지 및 하위 댓글도 삭제 */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        int deleteCommentCount = communityUpdateService.deletePost(postId, userEmail);
        String returnMsg = (deleteCommentCount == 0) ?
                "게시물이 삭제되었습니다" : "선택된 게시물과 하위 댓글 " + deleteCommentCount + "개가 삭제되었습니다";
        return ResponseEntity.ok(returnMsg);
    }

    /** 게시물 좋아요 API */
    @PostMapping("/posts/likes/{postId}")
    public ResponseEntity<?> likePost(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        communityLikeService.likePost(userEmail, postId);
        return ResponseEntity.ok("좋아요 성공");
    }

    /** 게시물 좋아요 취소 API */
    @DeleteMapping("/posts/likes/{postId}")
    public ResponseEntity<?> deleteLikePost(
            @PathVariable Integer postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        communityLikeService.deleteLikePost(userEmail, postId);
        return ResponseEntity.ok("좋아요 취소되었습니다.");
    }

    /** 게시물 신고 API */
    @PostMapping("/posts/reports/{postId}")
    public ResponseEntity<?> reportPost(
            @PathVariable Integer postId,
            @Valid @RequestBody ReportReqDTO reportDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        PostReportRespDTO reported = communityReportService.reportPost(userEmail, postId, reportDTO);
        return ResponseEntity.ok(reported);
    }

    /** 최종 게시글 생성 API */
    @PostMapping("/posts")
    public ResponseEntity<CommunityRespDTO> finalizePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PostFinalizeReqDTO finalizeReqDTO
    ) {
        String userEmail = userDetails.getUsername();
        CommunityRespDTO respDTO = communityUpdateService.finalizePost(userEmail, finalizeReqDTO);
        return ResponseEntity.ok(respDTO);
    }

    /** 게시물 검색 */
    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam(value = "category", required = false, defaultValue = "BOTH") String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<CommunityListDTO> postsPage = communitySearchService.search(category, keyword, page, size);
        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", postsPage.getNumber(),
                        "currentSize", postsPage.getContent().size(),
                        "totalPages", postsPage.getTotalPages(),
                        "totalSize", postsPage.getTotalElements()
                ),
                "items", postsPage.getContent()
        );
    }
}
