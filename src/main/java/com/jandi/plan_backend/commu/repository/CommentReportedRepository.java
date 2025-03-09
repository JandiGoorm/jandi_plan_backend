package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.CommentReported;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentReportedRepository extends JpaRepository<CommentReported, Long> {
    Optional<CommentReported> findByUser_userIdAndComment_CommentId(Integer userId, Integer commentId);

    // 신고된 이력이 있는 댓글을 중복없게 commentId 별로 그룹화해서 신고 횟수를 계산한 뒤,
    // 신고 횟수로 1차 정렬 후 동일 신고 횟수라면 commentId 역순 정렬해서 반환
    @Query("""
    SELECT comment.comment, COUNT(comment) FROM CommentReported comment GROUP BY comment.comment
    ORDER BY COUNT(comment) DESC, comment.comment.commentId DESC
    """)
    Page<Object[]> findReportedCommentsWithCount(Pageable pageable);

    Iterable<? extends CommentReported> findByComment_CommentId(Integer commentCommentId);

    List<CommentReported> findByUser_UserId(Integer userUserId);

    Iterable<? extends CommentReported> findByComment(Comment comment);
}
