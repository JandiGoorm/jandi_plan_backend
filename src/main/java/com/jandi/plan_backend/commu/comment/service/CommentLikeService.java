package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.entity.CommentLike;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
    private final ValidationUtil validationUtil;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

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
}
