package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.service.PostService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class PostController {

    private final PostService communityService;
    private final JwtTokenProvider jwtTokenProvider;

    public PostController(PostService postService, JwtTokenProvider jwtTokenProvider) {
        this.communityService = postService;
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

    /** 게시글 작성 API */
    @PostMapping("/posts")
    public ResponseEntity<?> writePost(
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommunityReqDTO postDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 게시글 저장 및 반환
        CommunityRespDTO savedPost = communityService.writePost(postDTO, userEmail);
        return ResponseEntity.ok(savedPost);
    }

    /** 게시물 수정 API */
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Integer postId, //경로 변수로 변경할 게시글 아이디 받기
            @RequestHeader("Authorization") String token, // 헤더의 Authorization에서 JWT 토큰 받기
            @RequestBody CommunityReqDTO postDTO // JSON 형식으로 게시글 작성 정보 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 게시물 수정 및 반환
        CommunityRespDTO updatedPost = communityService.updatePost(postDTO, postId, userEmail);
        return ResponseEntity.ok(updatedPost);
    }

    /** 게시물 삭제 API */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Integer postId,
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ){
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        // 게시물 삭제 및 반환
        int deleteCommentCount = communityService.deletePost(postId, userEmail);
        String returnMsg = (deleteCommentCount == 0) ?
                "게시물이 삭제되었습니다": "선택된 게시물과 하위 댓글 " + deleteCommentCount +"개가 삭제되었습니다";
        return ResponseEntity.ok(returnMsg);
    }

}
