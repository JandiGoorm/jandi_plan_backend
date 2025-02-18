package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.service.CommunityService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;
    private final JwtTokenProvider jwtTokenProvider;

    public CommunityController(CommunityService communityService, JwtTokenProvider jwtTokenProvider) {
        this.communityService = communityService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 게시물 목록 조회 API*/
    @GetMapping("/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<CommunityListDTO> postsPage = communityService.getAllPosts(page, size);

        return Map.of(
                "pageInfo", Map.of(
                        "currentPage", postsPage.getNumber(),  // 현재 페이지 번호
                        "currentSize", postsPage.getContent().size(), //현재 페이지 리스트 갯수
                        "totalPages", postsPage.getTotalPages(),  // 전체 페이지 번호 개수
                        "totalSize", postsPage.getTotalElements() // 전체 게시물 리스트 개수
                ),
                "items", postsPage.getContent()   // 현재 페이지의 게시물 데이터
        );
    }

    /** 특정 게시물 조회 API*/
    @GetMapping("/posts/{postId}")
    public Map<String, Object> getPosts(
            @PathVariable Integer postId //경로 변수로 게시물 고유 번호 받기
    ){
        return Map.of("items", communityService.getSpecPost(postId));
    }

    /** 댓글 조회 API */
    @GetMapping("/comments/{postId}")
    public Map<String, Object> getComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<ParentCommentDTO> parentCommentsPage = communityService.getAllComments(postId, page, size);

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
            @RequestParam(defaultValue = "10") int size
    ){
        Page<repliesDTO> repliesPage = communityService.getAllReplies(commentId, page, size);

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

    /** 게시글 작성 API */
    @PostMapping("/posts")
    public ResponseEntity<?> writePost(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommunityWritePostDTO postDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 게시글 저장 및 반환
        CommunityWriteRespDTO savedPost = communityService.writePost(postDTO, userEmail);
        return ResponseEntity.ok(savedPost);
    }

    /** 댓글 작성 API */
    @PostMapping("/comments/{postId}")
    public ResponseEntity<?> writeComment(
            @PathVariable Integer postId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommentWritePostDTO commentDTO // JSON 형식으로 게시글 작성 정보 받기
    ){

        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 댓글 저장 및 반환
        CommentWriteRespDTO savedComment = communityService.writeComment(commentDTO, postId, userEmail);
        return ResponseEntity.ok(savedComment);
    }

    /** 답글 작성 API */
    @PostMapping("/replies/{commentId}")
    public ResponseEntity<?> writeReplie(
            @PathVariable Integer commentId,
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommentWritePostDTO commentDTO // JSON 형식으로 게시글 작성 정보 받기
    ){

        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 댓글 저장 및 반환
        CommentWriteRespDTO savedComment = communityService.writeReplies(commentDTO, commentId, userEmail);
        return ResponseEntity.ok(savedComment);
    }

    /** 게시물 수정 API */
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Integer postId, //쿼리파라미터로 게시물 고유 번호 받기
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommunityWritePostDTO postDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 게시물 수정 및 반환
        CommunityWriteRespDTO updatedPost = communityService.updatePost(postDTO, postId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }

    /** 댓글 및 답글 수정 API */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Integer commentId, //쿼리파라미터로 게시물 고유 번호 받기
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommentWritePostDTO commentDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 게시물 수정 및 반환
        CommentWriteRespDTO updatedPost = communityService.updateComment(commentDTO, commentId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }
}
