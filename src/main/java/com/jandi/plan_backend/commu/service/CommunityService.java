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
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final ValidationUtil validationUtil;

    // 생성자를 통해 필요한 의존성들을 주입받음.
    public CommunityService(CommunityRepository communityRepository, CommentRepository commentRepository, ValidationUtil validationUtil) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.validationUtil = validationUtil;
    }

    /**
     * 특정 게시글 조회
     */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId) {
        //게시글의 존재 여부 검증
        Optional<Community> post = Optional.ofNullable(validationUtil.validatePostExists(postId));

        return post.map(CommunityItemDTO::new);
    }

    /**
     * 게시글 목록 전체 조회
     */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();
        return PaginationService.getPagedData(page, size, totalCount,
                communityRepository::findAll,
                CommunityListDTO::new);
    }


    /**
     * 댓글 목록 조회
     */
    public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size) {
        validationUtil.validatePostExists(postId); //게시글의 존재 여부 검증

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                ParentCommentDTO::new);
    }

    /**
     * 답글 목록 조회
     */
    public Page<repliesDTO> getAllReplies(Integer commentId, int page, int size) {
        validationUtil.validateCommentExists(commentId); //댓글의 존재 여부 검증

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                repliesDTO::new);
    }

    /**
     * 게시글 작성
     */
    public CommunityWriteRespDTO writePost(CommunityWritePostDTO postDTO, String userEmail) {
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
        return new CommunityWriteRespDTO(community);
    }

    /**
     * 댓글 작성
     */
    public CommentWriteRespDTO writeComment(CommentWritePostDTO commentDTO, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 게시글 검증
        Community post = validationUtil.validatePostExists(commentDTO.getPostId());

        // 댓글 검증
        Comments parentComment = (commentDTO.getParentCommentId() == null) ?
                null : validationUtil.validateCommentExists(commentDTO.getParentCommentId());

        // 댓글 생성
        Comments comment = new Comments();
        comment.setCommunity(post);
        comment.setParentComment(parentComment);
        comment.setUserId(user.getUserId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setContents(commentDTO.getContents());
        comment.setLikeCount(0);
        comment.setRepliesCount(0);


        // 댓글 저장
        commentRepository.save(comment);
        post.setCommentCount(post.getCommentCount() + 1); //게시글의 댓글 수 증가
        if (parentComment != null) { //답글인 경우 상위 댓글의 repliesCount 증가
            parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);
        }

        return new CommentWriteRespDTO(comment);
    }
}
