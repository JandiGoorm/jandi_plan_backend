package com.jandi.plan_backend.commu.community.controller;

import com.jandi.plan_backend.commu.community.dto.*;
import com.jandi.plan_backend.commu.community.service.*;
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
public class PostController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CommunityLikeService communityLikeService;
    private final CommunityQueryService communityQueryService;
    private final CommunityReportService communityReportService;
    private final CommunitySearchService communitySearchService;
    private final CommunityUpdateService communityUpdateService;

    public PostController(
            JwtTokenProvider jwtTokenProvider,
            CommunityLikeService communityLikeService,
            CommunityQueryService communityQueryService,
            CommunityReportService communityReportService,
            CommunitySearchService communitySearchService,
            CommunityUpdateService communityUpdateService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.communityLikeService = communityLikeService;
        this.communityQueryService = communityQueryService;
        this.communityReportService = communityReportService;
        this.communitySearchService = communitySearchService;
        this.communityUpdateService = communityUpdateService;
    }

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
            @RequestHeader("Authorization") String token,
            @RequestBody CommunityReqDTO postDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        CommunityRespDTO updatedPost = communityUpdateService.updatePost(postDTO, postId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }

    /** 게시물 삭제 API - 연결된 모든 이미지 및 하위 댓글도 삭제 */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Integer postId,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // PostService 내 deletePost 메서드에서
        // 1. 하위 댓글 삭제
        // 2. imageRepository.findAllByTargetTypeAndTargetId("community", postId)를 통해 모든 연결된 이미지를 조회하고,
        //    imageService.deleteImage(imageId)를 반복 호출하여 모두 삭제하도록 구현되어 있어야 합니다.
        int deleteCommentCount = communityUpdateService.deletePost(postId, userEmail);
        String returnMsg = (deleteCommentCount == 0) ?
                "게시물이 삭제되었습니다" : "선택된 게시물과 하위 댓글 " + deleteCommentCount + "개가 삭제되었습니다";
        return ResponseEntity.ok(returnMsg);
    }

    /** 게시물 좋아요 API */
    @PostMapping("/posts/likes/{postId}")
    public ResponseEntity<?> likePost(
            @PathVariable Integer postId,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        communityLikeService.likePost(userEmail, postId);
        return ResponseEntity.ok("좋아요 성공");
    }

    /** 게시물 좋아요 취소 API */
    @DeleteMapping("/posts/likes/{postId}")
    public ResponseEntity<?> deleteLikePost(
            @PathVariable Integer postId,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        communityLikeService.deleteLikePost(userEmail, postId);
        return ResponseEntity.ok("좋아요 취소되었습니다.");
    }

    /** 게시물 신고 API */
    @PostMapping("/posts/reports/{postId}")
    public ResponseEntity<?> reportPost(
            @PathVariable Integer postId,
            @RequestBody ReportReqDTO reportDTO,
            @RequestHeader("Authorization") String token
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        PostReportRespDTO reported = communityReportService.reportPost(userEmail, postId, reportDTO);
        return ResponseEntity.ok(reported);
    }

    /**
     * 최종 게시글 생성 API
     */
    @PostMapping("/posts")
    public ResponseEntity<CommunityRespDTO> finalizePost(
            @RequestHeader("Authorization") String token,
            @RequestBody PostFinalizeReqDTO finalizeReqDTO
    ) {
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

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
