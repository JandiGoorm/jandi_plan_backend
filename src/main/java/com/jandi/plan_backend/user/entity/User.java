package com.jandi.plan_backend.user.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.jandi.plan_backend.commu.community.entity.Community;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@EntityListeners(UserEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true, length = 50)
    private String userName;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean verified;

    @Column
    private String verificationToken;

    @Column
    private LocalDateTime tokenExpires;

    @Column
    private Boolean reported = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Community> communities;

    @Column(nullable = false)
    private int role;

    public Role getRoleEnum() {
        return Role.fromValue(this.role);
    }

    @Column(length = 20)
    private String socialType; // "KAKAO", "NAVER", "GOOGLE"

    @Column(length = 50)
    private String socialId;   // 카카오에서 받은 id값
}
