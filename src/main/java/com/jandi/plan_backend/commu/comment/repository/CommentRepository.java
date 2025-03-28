package com.jandi.plan_backend.commu.comment.repository;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.community.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    //특정 게시글의 부모 댓글만 조회하는 메서드
    Page<Comment> findByCommunityPostIdAndParentCommentIsNull(Integer postId, Pageable pageable);

    //특정 게시글에 속한 부모 댓글의 수를 반환하는 메서드
    long countByCommunityPostIdAndParentCommentIsNull(Integer postId);

    //특정 댓글에 속한 자식 답글만 조회하는 메서드
    Page<Comment> findByParentCommentCommentId(Integer commentId, Pageable pageable);

    //특정 댓글에 속한 자식 답글의 수를 반환하는 메서드
    long countByParentCommentCommentId(Integer commentId);

    //댓글이 존재하는지 조회하는 메서드
    Optional<Object> findByCommentId(Integer commentId);

    //특정 댓글에 속한 자식 답글을 모두 조회하는 메서드
    List<Comment> findByParentCommentCommentId(Integer commentId);

    List<Comment> findByCommunity(Community community);

    // 특정 유저의 댓글을 조회하는 메서드
    List<Comment> findByUserId(Integer userId);

    List<Comment> findByUserIdAndParentCommentIsNull(Integer userId);

    List<Comment> findByUserIdAndParentCommentIsNotNull(Integer userId);

    /** 증감 쿼리 */
    // 게시글 댓글 수 증가
    @Modifying
    @Query("update Community c set c.commentCount = c.commentCount + :count where c.postId = :postId")
    void increaseCommentCount(@Param("postId") Integer postId, @Param("count") int count);

    // 게시글 댓글 수 감소
    @Modifying
    @Query("update Community c set c.commentCount = c.commentCount - :count where c.postId = :postId")
    void decreaseCommentCount(@Param("postId") Integer postId, @Param("count") int count);

    // 부모 댓글의 답글 수 증가
    @Modifying
    @Query("update Comment c set c.repliesCount = c.repliesCount + 1 where c.commentId = :commentId")
    void increaseRepliesCount(@Param("commentId") Integer commentId);

    // 부모 댓글의 답글 수 감소
    @Modifying
    @Query("update Comment c set c.repliesCount = c.repliesCount - 1 where c.commentId = :commentId")
    void decreaseRepliesCount(@Param("commentId") Integer commentId);
}
