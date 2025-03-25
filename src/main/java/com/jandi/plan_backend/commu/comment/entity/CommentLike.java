package com.jandi.plan_backend.commu.comment.entity;

import com.jandi.plan_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_like")
@IdClass(CommentLikeId.class)
@Data
public class CommentLike {
    @Id
    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
