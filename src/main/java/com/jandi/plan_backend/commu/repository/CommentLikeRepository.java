package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.CommentLike;
import com.jandi.plan_backend.commu.entity.CommentLikeId;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    //특정 유저가 특정 댓글을 좋아요했는지 검색
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);

    Iterable<? extends CommentLike> findByComment(Comment comment);

    List<CommentLike> findByComment_CommentId(Integer commentId);

    Iterable<? extends CommentLike> findByUser(User user);
}
