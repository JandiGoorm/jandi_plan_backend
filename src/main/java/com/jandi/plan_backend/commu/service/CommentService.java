package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.CommentLike;
import com.jandi.plan_backend.commu.entity.CommentReported;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReportedRepository commentReportedRepository;

    // 생성자를 통해 필요한 의존성들을 주입받음.
    public CommentService(CommunityRepository communityRepository, CommentRepository commentRepository, ValidationUtil validationUtil, ImageService imageService, CommentLikeRepository commentLikeRepository, CommentReportedRepository commentReportedRepository) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.commentLikeRepository = commentLikeRepository;
        this.commentReportedRepository = commentReportedRepository;
    }

    /** 댓글 목록 조회 */
    public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size) {
        validationUtil.validatePostExists(postId); //게시글의 존재 여부 검증

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                comment -> new ParentCommentDTO(comment, validationUtil.validateUserExists(comment.getUserId()), imageService)
        );
    }

    /** 답글 목록 조회 */
    public Page<RepliesDTO> getAllReplies(Integer commentId, int page, int size) {
        validationUtil.validateCommentExists(commentId); //댓글의 존재 여부 검증

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                reply -> new RepliesDTO(reply, validationUtil.validateUserExists(reply.getUserId()), imageService)
        );
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
        Comment parentComment = validationUtil.validateCommentExists(commentId);
        Community post = parentComment.getCommunity();

        //추가된 답글 반환
        return saveComment(user, parentComment, post, commentDTO.getContents());
    }

    // 대댓글 실제 저장
    private CommentRespDTO saveComment(User user, Comment parentComment, Community post, String content){
        // 댓글 생성 및 저장
        Comment comment = new Comment();
        comment.setCommunity(post);
        comment.setParentComment(parentComment);
        comment.setUserId(user.getUserId());
        comment.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
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

    /** 댓글 수정 */
    public CommentRespDTO updateComment(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        // 댓글 검증
        Comment comment = validationUtil.validateCommentExists(commentId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfComment(user, comment);

        //댓글 수정
        comment.setContents(commentDTO.getContents());

        // DB 저장 및 반환
        commentRepository.save(comment);
        return new CommentRespDTO(comment);
    }

    /**
     * 댓글 및 답글 삭제
     */
    public int deleteComments(String userEmail, Integer commentId) {
        // 댓글 검증
        Comment comment = validationUtil.validateCommentExists(commentId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        if(user.getUserId()!=1) validationUtil.validateUserIsAuthorOfComment(user, comment);

        int repliesCount = 0;
        if(comment.getParentComment() == null){
            // 만약 댓글일 경우 하위 답글 모두 삭제
            List<Comment> replies = commentRepository.findByParentCommentCommentId(commentId);
            repliesCount = replies.size();
            commentRepository.deleteAll(replies);
        }else{
            // 삭제하는 게 답글인 경우 상위 댓글의 답글 수 반영
            Comment parentComment = comment.getParentComment();
            parentComment.setRepliesCount(parentComment.getRepliesCount() - 1);
            commentRepository.save(parentComment);
        }
        // 게시글에 댓글 수 반영
        Community post = comment.getCommunity();
        post.setCommentCount(post.getCommentCount() - 1 - repliesCount);
        communityRepository.save(post);

        // 댓글 삭제 및 반환
        commentRepository.delete(comment);
        return repliesCount;
    }

    /** 댓글 좋아요 */
    public void likeComment(String userEmail, Integer commentId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 댓글 검증
        Comment comment = validationUtil.validateCommentExists(commentId);

        // 좋아요 검증
        if(user.getUserId().equals(comment.getUserId())){ // 셀프 좋아요 방지
            throw new BadRequestExceptionMessage("본인의 댓글에 좋아요할 수 없습니다");
        }
        if(commentLikeRepository.findByCommentAndUser(comment, user).isPresent()){ //중복 좋아요 방지
            throw new BadRequestExceptionMessage("이미 좋아요한 댓글입니다.");
        }

        // commentLike에 반영
        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLike.setUser(user);
        commentLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        commentLikeRepository.save(commentLike);

        // 댓글 좋아요 수 증가
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    /** 댓글 좋아요 취소 */
    public void deleteLikeComment(String userEmail, Integer commentId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 댓글 검증
        Comment comment = validationUtil.validateCommentExists(commentId);

        // 좋아요 검증
        Optional<CommentLike> commentLike = commentLikeRepository.findByCommentAndUser(comment, user);
        if(commentLike.isEmpty()){ // 좋아요되지 않은 댓글일 때
            throw new BadRequestExceptionMessage("좋아요한 적 없는 게시물입니다.");
        }

        commentLikeRepository.delete(commentLike.get());
        comment.setLikeCount(comment.getLikeCount() - 1);
        commentRepository.save(comment);
    }

    public CommentReportRespDTO reportComment(String userEmail, Integer commentId, ReportReqDTO reportDTO) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 댓글 검증
        Comment comment = validationUtil.validateCommentExists(commentId);

        // 중복 신고 방지
        if(commentReportedRepository.findByUser_userIdAndComment_CommentId(user.getUserId(), commentId).isPresent()){
            throw new BadRequestExceptionMessage("이미 신고한 댓글입니다");
        }

        // 댓글 신고
        CommentReported commentReported = new CommentReported();
        commentReported.setComment(comment);
        commentReported.setUser(user);
        commentReported.setContents(reportDTO.getContents());
        commentReported.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        commentReportedRepository.save(commentReported);

        return new CommentReportRespDTO(commentReported);
    }
}
