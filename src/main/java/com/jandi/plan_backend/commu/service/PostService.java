package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.jandi.plan_backend.util.service.PaginationService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final ValidationUtil validationUtil;

    // 생성자를 통해 필요한 의존성들을 주입받음.
    public PostService(CommunityRepository communityRepository, CommentRepository commentRepository, ValidationUtil validationUtil) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.validationUtil = validationUtil;
    }

    /** 특정 게시글 조회 */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId) {
        //게시글의 존재 여부 검증
        Optional<Community> post = Optional.ofNullable(validationUtil.validatePostExists(postId));

        return post.map(CommunityItemDTO::new);
    }

    /** 게시글 목록 전체 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();
        return PaginationService.getPagedData(page, size, totalCount,
                communityRepository::findAll,
                CommunityListDTO::new);
    }

    /** 게시글 작성 */
    public CommunityRespDTO writePost(CommunityReqDTO postDTO, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 게시글 생성
        Community community = new Community();
        community.setUser(user);
        community.setTitle(postDTO.getTitle());
        community.setContents(postDTO.getContent());
        community.setCreatedAt(LocalDateTime.now());
        community.setLikeCount(0);
        community.setCommentCount(0);

        // DB 저장 및 반환
        communityRepository.save(community);
        return new CommunityRespDTO(community);
    }


    /** 게시물 수정 */
    public CommunityRespDTO updatePost(CommunityReqDTO postDTO, Integer postId, String userEmail) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        // 게시글 수정
        post.setTitle(postDTO.getTitle());
        post.setContents(postDTO.getContent());

        // DB 저장 및 반환
        communityRepository.save(post);
        return new CommunityRespDTO(post);
    }

}
