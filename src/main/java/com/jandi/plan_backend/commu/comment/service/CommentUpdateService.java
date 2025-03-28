package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.dto.CommentReqDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentRespDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentUpdateService {
    private final ValidationUtil validationUtil;
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final ImageService imageService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReportedRepository commentReportedRepository;

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
}
