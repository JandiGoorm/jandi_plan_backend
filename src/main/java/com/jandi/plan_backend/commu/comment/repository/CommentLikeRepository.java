package com.jandi.plan_backend.commu.comment.repository;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.entity.CommentLike;
import com.jandi.plan_backend.commu.comment.entity.CommentLikeId;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    //특정 유저가 특정 댓글을 좋아요했는지 검색
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    Iterable<? extends CommentLike> findByComment(Comment comment);

    List<CommentLike> findByComment_CommentId(Integer commentId);

    Iterable<? extends CommentLike> findByUser(User user);

    /**
     * 특정 유저가 여러 댓글에 좋아요했는지 한 번에 조회 (N+1 방지)
     */
    @Query("SELECT cl.comment.commentId FROM CommentLike cl WHERE cl.user = :user AND cl.comment.commentId IN :commentIds")
    Set<Integer> findLikedCommentIdsByUserAndCommentIds(
            @Param("user") User user,
            @Param("commentIds") List<Integer> commentIds);
}
