package com.jandi.plan_backend.commu.comment.repository;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.community.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
