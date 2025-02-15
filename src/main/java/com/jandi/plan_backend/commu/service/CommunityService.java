package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.jandi.plan_backend.util.service.PaginationService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;


    // 생성자를 통해 필요한 의존성들을 주입받음.
    public CommunityService(CommunityRepository communityRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    /** 특정 게시글 조회 */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId) {
        //게시글의 존재 여부 검증
        Optional<Community> post = Optional.ofNullable(validatePostExists(postId));

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
        validatePostExists(postId); //게시글의 존재 여부 검증

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                ParentCommentDTO::new);
    }

    /** 답글 목록 조회 */
    public Page<repliesDTO> getAllReplies(Integer commentId, int page, int size) {
        validateCommentExists(commentId); //댓글의 존재 여부 검증

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                repliesDTO::new);
    }

    /** 게시글 작성 */
    public CommunityWriteRespDTO writePost(CommunityWritePostDTO postDTO, String userEmail) {
        // 유저 검증
        User user = validateUserExists(userEmail);
        validateUserRestricted(user);

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

    /** 댓글 작성 */
    public CommentWriteRespDTO writeComment(CommentWritePostDTO commentDTO, String userEmail){
        // 유저 검증
        User user = validateUserExists(userEmail);
        validateUserRestricted(user);

        // 게시글 검증
        Community post = validatePostExists(commentDTO.getPostId());

        // 댓글 검증
        Comments parentComment = (commentDTO.getParentCommentId() == null) ?
                null : validateCommentExists(commentDTO.getParentCommentId());

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
        if(parentComment != null) { //답글인 경우 상위 댓글의 repliesCount 증가
            parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);
        }

        return new CommentWriteRespDTO(comment);
    }

    /**
     * 검증 검사 메서드
     *
     * @return
     */
    // 게시글의 존재 여부 검증
    private Community validatePostExists(Integer postId) {
        return communityRepository.findByPostId(postId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 게시글입니다."));
    }

    // 댓글의 존재 여부 검증
    private Comments validateCommentExists(Integer commentId) {
        return (Comments) commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 댓글입니다."));
    }

    // 사용자의 존재 여부 검증
    private User validateUserExists(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    // 사용자 활동 제한 여부 검증
    private void validateUserRestricted(User user) {
        if (user.getReported()) {
            throw new BadRequestExceptionMessage("비정상적인 활동이 반복되어 게시글 작성이 제한되었습니다.");
        }
    }
}
