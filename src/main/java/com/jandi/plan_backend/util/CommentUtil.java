package com.jandi.plan_backend.util;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentUtil {
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    public boolean isLiked(Comment comment, User user) {
        return (user != null && commentLikeRepository.findByCommentAndUser(comment, user).isPresent());
    }

    public User getCommentUser(Comment comment) {
        Integer userId = comment.getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("아이디를 찾을 수 없습니다: " + userId));
    }

    /**
     * 여러 댓글의 작성자를 한 번에 조회 (N+1 방지)
     * @param comments 댓글 목록
     * @return userId -> User 맵
     */
    public Map<Integer, User> getCommentUsersMap(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        return userRepository.findAllByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
    }

    /**
     * 특정 유저가 여러 댓글에 좋아요했는지 한 번에 조회 (N+1 방지)
     * @param comments 댓글 목록
     * @param user 현재 유저 (null 가능)
     * @return 좋아요한 commentId Set
     */
    public Set<Integer> getLikedCommentIds(List<Comment> comments, User user) {
        if (user == null || comments == null || comments.isEmpty()) {
            return Collections.emptySet();
        }

        List<Integer> commentIds = comments.stream()
                .map(Comment::getCommentId)
                .collect(Collectors.toList());

        return commentLikeRepository.findLikedCommentIdsByUserAndCommentIds(user, commentIds);
    }
}
