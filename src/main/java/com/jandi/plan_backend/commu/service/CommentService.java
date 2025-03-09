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
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CommentService {
    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReportedRepository commentReportedRepository;
    private final UserRepository userRepository; // 최신 사용자 정보를 조회하기 위함

    public CommentService(CommunityRepository communityRepository,
                          CommentRepository commentRepository,
                          ValidationUtil validationUtil,
                          ImageService imageService,
                          CommentLikeRepository commentLikeRepository,
                          CommentReportedRepository commentReportedRepository,
                          UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.commentLikeRepository = commentLikeRepository;
        this.commentReportedRepository = commentReportedRepository;
        this.userRepository = userRepository;
    }

    /** 댓글 목록 조회 */
    public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size, String userEmail) {
        validationUtil.validatePostExists(postId); // 게시글 존재 검증

        // 현재 요청자의 최신 정보를 조회 (로그인이 되어 있다면)
        User currentUser = (userEmail != null)
                ? userRepository.findByEmail(userEmail).orElse(null)
                : null;

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                comment -> {
                    // 최신 사용자 정보 (댓글 작성자) 조회
                    User commentAuthor = userRepository.findById(comment.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + comment.getUserId()));
                    boolean liked = (currentUser != null &&
                            commentLikeRepository.findByCommentAndUser(comment, currentUser).isPresent());
                    return new ParentCommentDTO(comment, commentAuthor, imageService, liked);
                });
    }

    /** 답글 목록 조회 */
    public Page<RepliesDTO> getAllReplies(Integer commentId, int page, int size, String userEmail) {
        validationUtil.validateCommentExists(commentId); // 댓글 존재 검증

        User currentUser = (userEmail != null)
                ? userRepository.findByEmail(userEmail).orElse(null)
                : null;

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                reply -> {
                    User replyAuthor = userRepository.findById(reply.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + reply.getUserId()));
                    boolean liked = (currentUser != null &&
                            commentLikeRepository.findByCommentAndUser(reply, currentUser).isPresent());
                    return new RepliesDTO(reply, replyAuthor, imageService, liked);
                });
    }

    /** 댓글 작성 */
    public CommentRespDTO writeComment(CommentReqDTO commentDTO, Integer postId, String userEmail) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Community post = validationUtil.validatePostExists(postId);
        return saveComment(user, null, post, commentDTO.getContents());
    }

    /** 답글 작성 */
    public CommentRespDTO writeReplies(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Comment parentComment = validationUtil.validateCommentExists(commentId);
        Community post = parentComment.getCommunity();
        return saveComment(user, parentComment, post, commentDTO.getContents());
    }

    // 댓글 저장 메서드 (댓글 또는 답글)
    private CommentRespDTO saveComment(User user, Comment parentComment, Community post, String content) {
        Comment comment = new Comment();
        comment.setCommunity(post);
        comment.setParentComment(parentComment);
        comment.setUserId(user.getUserId());
        comment.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        comment.setContents(content);
        comment.setLikeCount(0);
        comment.setRepliesCount(0);
        commentRepository.save(comment);

        if (parentComment != null) {
            parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);
            commentRepository.save(parentComment);
        }
        post.setCommentCount(post.getCommentCount() + 1);
        communityRepository.save(post);

        return new CommentRespDTO(comment, imageService);
    }

    /** 댓글 수정 */
    public CommentRespDTO updateComment(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        Comment comment = validationUtil.validateCommentExists(commentId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfComment(user, comment);
        comment.setContents(commentDTO.getContents());
        commentRepository.save(comment);
        return new CommentRespDTO(comment, imageService);
    }

    /**
     * 댓글 및 답글 삭제
     */
    public int deleteComments(Integer commentId, String userEmail) {
        Comment comment = validationUtil.validateCommentExists(commentId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfComment(user, comment);

        int repliesCount = 0;
        if (comment.getParentComment() == null) {
            log.info("댓글 삭제: {}", commentId);

            // 답글의 좋아요 및 신고 정보 삭제 후 답글 삭제
            List<Comment> replies = commentRepository.findByParentCommentCommentId(commentId);
            repliesCount = replies.size();
            for(Comment reply : replies) {
                log.info("하위 댓글 삭제: {}", reply.getCommentId());
                commentLikeRepository.deleteAll(commentLikeRepository.findByComment_CommentId(reply.getCommentId()));
                commentReportedRepository.deleteAll(commentReportedRepository.findByComment_CommentId(reply.getCommentId()));
            }
            commentRepository.deleteAll(replies);
        } else {
            log.info("답글 삭제: {}", commentId);
            Comment parentComment = comment.getParentComment();
            parentComment.setRepliesCount(parentComment.getRepliesCount() - 1);
            commentRepository.save(parentComment);
        }
        Community post = comment.getCommunity();
        post.setCommentCount(post.getCommentCount() - 1 - repliesCount);
        communityRepository.save(post);

        // 자신의 좋아요 및 신고 정보 삭제 후 자신 삭제
        commentLikeRepository.deleteAll(commentLikeRepository.findByComment(comment));
        commentReportedRepository.deleteAll(commentReportedRepository.findByComment(comment));
        commentRepository.delete(comment);
        return repliesCount;
    }

    /** 댓글 좋아요 */
    public void likeComment(String userEmail, Integer commentId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Comment comment = validationUtil.validateCommentExists(commentId);
        if (user.getUserId().equals(comment.getUserId())) {
            throw new BadRequestExceptionMessage("본인의 댓글에 좋아요할 수 없습니다");
        }
        if (commentLikeRepository.findByCommentAndUser(comment, user).isPresent()) {
            throw new BadRequestExceptionMessage("이미 좋아요한 댓글입니다.");
        }
        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLike.setUser(user);
        commentLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        commentLikeRepository.save(commentLike);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }

    /** 댓글 좋아요 취소 */
    public void deleteLikeComment(String userEmail, Integer commentId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Comment comment = validationUtil.validateCommentExists(commentId);
        Optional<CommentLike> commentLike = commentLikeRepository.findByCommentAndUser(comment, user);
        if (commentLike.isEmpty()) {
            throw new BadRequestExceptionMessage("좋아요한 적 없는 댓글입니다.");
        }
        commentLikeRepository.delete(commentLike.get());
        comment.setLikeCount(comment.getLikeCount() - 1);
        commentRepository.save(comment);
    }

    /** 댓글 신고 */
    public CommentReportRespDTO reportComment(String userEmail, Integer commentId, ReportReqDTO reportDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Comment comment = validationUtil.validateCommentExists(commentId);
        if (commentReportedRepository.findByUser_userIdAndComment_CommentId(user.getUserId(), commentId).isPresent()) {
            throw new BadRequestExceptionMessage("이미 신고한 댓글입니다");
        }
        CommentReported commentReported = new CommentReported();
        commentReported.setComment(comment);
        commentReported.setUser(user);
        commentReported.setContents(reportDTO.getContents());
        commentReported.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        commentReportedRepository.save(commentReported);
        return new CommentReportRespDTO(commentReported);
    }
}
