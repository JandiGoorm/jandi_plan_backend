package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.service.PostService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class PostController {

    private final PostService communityService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PostService postService;

    public PostController(PostService postService, JwtTokenProvider jwtTokenProvider) {
        this.communityService = postService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.postService = postService;
    }

    /** 게시물 목록 조회 API */
    @GetMapping("/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CommunityListDTO> postsPage = communityService.getAllPosts(page, size);
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
            @PathVariable Integer postId
    ) {
        return Map.of("items", communityService.getSpecPost(postId));
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
        CommunityRespDTO updatedPost = communityService.updatePost(postDTO, postId, userEmail);
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
        // PostService 내 deletePost 메서드에서
        // 1. 하위 댓글 삭제
        // 2. imageRepository.findAllByTargetTypeAndTargetId("community", postId)를 통해 모든 연결된 이미지를 조회하고,
        //    imageService.deleteImage(imageId)를 반복 호출하여 모두 삭제하도록 구현되어 있어야 합니다.
        int deleteCommentCount = communityService.deletePost(postId, userEmail);
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
        postService.likePost(userEmail, postId);
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
        postService.deleteLikePost(userEmail, postId);
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

        PostReportRespDTO reported = postService.reportPost(userEmail, postId, reportDTO);
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
        CommunityRespDTO respDTO = postService.finalizePost(userEmail, finalizeReqDTO);
        return ResponseEntity.ok(respDTO);
    }
}
