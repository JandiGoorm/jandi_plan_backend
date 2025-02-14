package com.jandi.plan_backend.commu.controller;

import com.jandi.plan_backend.commu.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.service.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    /** 페이지 단위로 게시물 리스트 조회 */
    @GetMapping("/posts")
    public Map<String, Object> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
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

    /** 페이지 단위로 특정 게시물의 댓글만 가져오는 API */
    @GetMapping("/comments")
    public Map<String, Object> getParentComments(
            @RequestParam(required = false) Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ParentCommentDTO> parentCommentsPage = communityService.getParentComments(postId, page, size);

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
}
