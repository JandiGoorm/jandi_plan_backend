package com.jandi.plan_backend.commu.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "community")
@Data
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(length = 200, nullable = false)
    private String preview;

    @Convert(converter = CommunityHashtagConverter.class)
    @Column(columnDefinition = "JSON")
    private List<String> hashtags;

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private Integer commentCount;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
}

