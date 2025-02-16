package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.service.CommunityService;
import com.jandi.plan_backend.user.security.JwtTokenProvider;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;
    private final JwtTokenProvider jwtTokenProvider; // 추가


    public CommunityController(CommunityService communityService, JwtTokenProvider jwtTokenProvider) {
        this.communityService = communityService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 게시물 조회 API*/
    @GetMapping("/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer postId
    ){
        return postId != null ?
                getSpecPost(postId) : //postId 입력 시 특정 게시글 조회
                getAllPosts(page, size); //postId 미입력 시 게시글 목록 전체 조회
    }

    //특정 게시글 조회
    public Map<String, Object> getSpecPost(Integer postId){
        return Map.of("items", communityService.getSpecPost(postId));
    }

    //게시글 목록 전체 조회
    public Map<String, Object> getAllPosts(int page, int size){
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



    /** 댓글 조회 API */
    @GetMapping("/comments")
    public Map<String, Object> getComments(
            @RequestParam(required = false) Integer postId,
            @RequestParam(required = false) Integer commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        if(postId != null){
            return getAllComments(postId, page, size); //postId 입력 시 해당 게시글의 댓글 목록 조회
        }else if(commentId != null){
            return getAllReplies(commentId, page, size); //commentId 입력 시 해당 댓글의 답글 목록 조회
        }

        //아무 파라미터도 넘기지 않았을 때 에러 처리
        throw new BadRequestExceptionMessage("postId 또는 commentId를 반드시 입력해야 합니다.");
    }

    // 댓글 목록 조회
    public Map<String, Object> getAllComments(int postId, int page, int size){
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

    // 답글 목록 조회
    public Map<String, Object> getAllReplies( int commentId, int page, int size){
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

    @PostMapping("/posts/write/post")
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
}
