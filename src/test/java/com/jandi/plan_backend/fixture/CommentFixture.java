package com.jandi.plan_backend.fixture;

import com.jandi.plan_backend.commu.comment.dto.CommentReqDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Comment 관련 테스트 데이터 팩토리 클래스
 */
public class CommentFixture {

    private static final LocalDateTime NOW = TimeUtil.now();

    /**
     * 기본 댓글 생성
     */
    public static Comment createComment(User user, Community community) {
        Comment comment = new Comment();
        comment.setCommentId(1);
        comment.setUserId(user.getUserId());
        comment.setCommunity(community);
        comment.setParentComment(null);
        comment.setContents("테스트 댓글 내용입니다.");
        comment.setLikeCount(0);
        comment.setRepliesCount(0);
        comment.setCreatedAt(NOW);
        return comment;
    }

    /**
     * 특정 ID를 가진 댓글 생성
     */
    public static Comment createCommentWithId(Integer commentId, User user, Community community) {
        Comment comment = createComment(user, community);
        comment.setCommentId(commentId);
        comment.setContents("테스트 댓글 " + commentId);
        return comment;
    }

    /**
     * 답글 생성 (부모 댓글 포함)
     */
    public static Comment createReply(User user, Community community, Comment parentComment) {
        Comment reply = new Comment();
        reply.setCommentId(100);
        reply.setUserId(user.getUserId());
        reply.setCommunity(community);
        reply.setParentComment(parentComment);
        reply.setContents("테스트 답글 내용입니다.");
        reply.setLikeCount(0);
        reply.setRepliesCount(0);
        reply.setCreatedAt(NOW);
        return reply;
    }

    /**
     * 댓글 작성 요청 DTO
     */
    public static CommentReqDTO createCommentReqDTO() {
        return new CommentReqDTO("새로운 댓글 내용입니다.");
    }

    /**
     * 댓글 수정 요청 DTO
     */
    public static CommentReqDTO createUpdateReqDTO() {
        return new CommentReqDTO("수정된 댓글 내용입니다.");
    }

    /**
     * 여러 댓글 목록 생성
     */
    public static List<Comment> createCommentList(User user, Community community, int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> createCommentWithId(i, user, community))
                .toList();
    }
}
