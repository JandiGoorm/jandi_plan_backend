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


    /** 댓글 목록 조회 */
    public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size) {
        validationUtil.validatePostExists(postId); //게시글의 존재 여부 검증

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                ParentCommentDTO::new);
    }

    /** 답글 목록 조회 */
    public Page<repliesDTO> getAllReplies(Integer commentId, int page, int size) {
        validationUtil.validateCommentExists(commentId); //댓글의 존재 여부 검증

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                repliesDTO::new);
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

    /** 댓글 작성 */
    public CommentRespDTO writeComment(CommentReqDTO commentDTO, Integer postId, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        //추가된 답글 반환
        return saveComment(user, null, post, commentDTO.getContents());
    }

    /** 답글 작성 */
    public CommentRespDTO writeReplies(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 댓글 검증
        Comments parentComment = validationUtil.validateCommentExists(commentId);
        Community post = parentComment.getCommunity();

        //추가된 답글 반환
        return saveComment(user, parentComment, post, commentDTO.getContents());
    }

    // 대댓글 실제 저장
    private CommentRespDTO saveComment(User user, Comments parentComment, Community post, String content){
        // 댓글 생성 및 저장
        Comments comment = new Comments();
        comment.setCommunity(post);
        comment.setParentComment(parentComment);
        comment.setUserId(user.getUserId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setContents(content);
        comment.setLikeCount(0);
        comment.setRepliesCount(0);
        commentRepository.save(comment);

        //카운트 반영
        if(parentComment != null){ // 부모 댓글의 답글 수 증가
            parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);
            commentRepository.save(parentComment);
        }
        post.setCommentCount(post.getCommentCount() + 1); // 게시글의 댓글 수 증가
        communityRepository.save(post);

        return new CommentRespDTO(comment);
    }

    /** 게시물 수정 */
    public CommunityRespDTO updatePost(CommunityReqDTO postDTO, Integer postId, String userEmail) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        // 게시글 수정: 빈 값이 아닐 때만 수정되게 함
        if(postDTO.getTitle()!=null) post.setTitle(postDTO.getTitle());
        if(postDTO.getContent()!=null) post.setContents(postDTO.getContent());

        // DB 저장 및 반환
        communityRepository.save(post);
        return new CommunityRespDTO(post);
    }

    /** 댓글 수정 */
    public CommentRespDTO updateComment(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        // 댓글 검증
        Comments comment = validationUtil.validateCommentExists(commentId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfComment(user, comment);

        //댓글 수정: 빈 값이 아닐 때만 수정되게 함
        if(commentDTO.getContents()!=null) comment.setContents(commentDTO.getContents());

        // DB 저장 및 반환
        commentRepository.save(comment);
        return new CommentRespDTO(comment);
    }
}
