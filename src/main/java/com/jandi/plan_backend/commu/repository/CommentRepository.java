package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comments, Integer> {

    //특정 게시글의 부모 댓글만 조회하는 메서드
    Page<Comments> findByCommunityPostIdAndParentCommentIsNull(Integer postId, Pageable pageable);
}
