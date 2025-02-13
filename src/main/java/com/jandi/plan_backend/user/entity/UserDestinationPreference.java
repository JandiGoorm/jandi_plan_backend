package com.jandi.plan_backend.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 사용자 선호 주요 여행지 정보 테이블 (user_destination_preference)
 *
 * 이 엔티티는 사용자가 선호하는 주요 여행지 정보를 저장하는 테이블을 매핑한다.
 * 테이블의 기본키는 복합키(user_id, destination_id)로 구성되며,
 * 각각 User 엔티티와 MajorDestination 엔티티와 다대일 관계를 가진다.
 *
 * 필드 설명:
 * - user: 사용자 정보(User 엔티티). 사용자 ID를 참조하며, 복합키의 일부임.
 * - majorDestination: 주요 여행지 정보(MajorDestination 엔티티). 여행지 ID를 참조하며, 복합키의 일부임.
 * - createdAt: 이 선호 정보가 생성된 시각을 저장.
 */
@Entity
@Table(name = "user_destination_preference")
@IdClass(UserDestinationPreferenceId.class)
@Data
public class UserDestinationPreference {

    // 사용자 정보를 참조. User 엔티티와 다대일 관계이며, 복합 기본키의 첫 번째 부분.
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 주요 여행지 정보를 참조. MajorDestination 엔티티와 다대일 관계이며, 복합 기본키의 두 번째 부분.
    @Id
    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private MajorDestination majorDestination;

    // 이 레코드가 생성된 시간을 저장. 선호 정보가 언제 등록되었는지 기록.
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
