package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.CommentReported;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentReportedRepository extends JpaRepository<CommentReported, Long> {
    Optional<CommentReported> findByUser_userIdAndComment_CommentId(Integer userId, Integer commentId);
}
