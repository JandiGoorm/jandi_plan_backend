package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comments, Integer> {

    //특정 게시글의 부모 댓글만 조회하는 메서드
    Page<Comments> findByCommunityPostIdAndParentCommentIsNull(Integer postId, Pageable pageable);

    //특정 게시글에 속한 부모 댓글의 수를 반환하는 메서드
    long countByCommunityPostIdAndParentCommentIsNull(Integer postId);

    //특정 댓글에 속한 자식 답글만 조회하는 메서드
    Page<Comments> findByParentCommentCommentId(Integer commentId, Pageable pageable);

    //특정 댓글에 속한 자식 답글의 수를 반환하는 메서드
    long countByParentCommentCommentId(Integer commentId);

    //댓글이 존재하는지 조회하는 메서드
    Optional<Object> findByCommentId(Integer commentId);
}
