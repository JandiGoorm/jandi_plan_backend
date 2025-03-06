package com.jandi.plan_backend.trip.entity;

import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.itinerary.entity.Itinerary;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "trip")
@Data
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean privatePlan;

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private Integer budget;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    /**
     * 생성자: 엔티티 생성 시 기본 필드 초기화
     */
    public Trip(User user, String title, Boolean privatePlan,
                LocalDate startDate, LocalDate endDate, Integer budget, City city) {
        this.user = user;
        this.title = title;
        this.privatePlan = privatePlan;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.city = city;
    }

    public Trip() {

    }

    /**
     * 엔티티가 처음 저장되기 전에 자동 실행됨.
     * createdAt, updatedAt, likeCount 기본값 설정
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
    }

    /**
     * 엔티티가 업데이트될 때 updatedAt 자동 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    /**
     * TripLike 엔티티와의 1:N 관계.
     * Trip 삭제 시 TripLike도 자동으로 삭제되도록 cascade와 orphanRemoval 설정.
     */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripLike> tripLikes;

    /**
     * TripParticipant 엔티티와의 1:N 관계.
     * Trip 삭제 시 TripParticipant도 자동으로 삭제되도록 cascade와 orphanRemoval 설정.
     */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> participants;

    /**
     * Itinerary 엔티티와의 1:N 관계.
     * Trip 삭제 시 연관 일정도 자동으로 삭제됩니다.
     */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerary> itineraries;

    /**
     * Reservation 엔티티와의 1:N 관계.
     * Trip 삭제 시 연관 예약도 자동으로 삭제됩니다.
     */
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;
}
