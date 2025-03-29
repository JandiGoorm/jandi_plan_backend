package com.jandi.plan_backend.util;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
