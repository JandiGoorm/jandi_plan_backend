package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.trip.entity.Trip;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 엔티티 클래스.
 * "expense" 테이블과 매핑되며, 여행 경비(비용) 정보를 저장함.
 *
 * 필드 설명:
 * - expenseId: 경비 정보의 고유 식별자(PK). 데이터베이스가 자동으로 증가시킴.
 * - trip: 경비가 속한 여행(Trip)을 나타냄. 여러 경비 정보가 하나의 여행에 속할 수 있음.
 * - itinerary: 경비가 특정 일정에 해당하는 경우 해당 일정(Itinerary)을 나타냄.
 *               여행 전체 경비라면 null이 될 수 있음.
 * - category: 경비의 분류 정보. 예를 들어, "교통", "식사", "숙박" 등의 값을 가짐.
 *             최대 길이 50자이며 null 값을 허용하지 않음.
 * - amount: 경비 금액을 나타내며, 소수점 이하 2자리까지 저장할 수 있는 BigDecimal 타입.
 *           null 값을 허용하지 않음.
 * - description: 경비에 대한 상세 설명. TEXT 타입으로 저장되어 길이 제한이 없음.
 * - createdAt: 경비 정보가 생성된 시각을 나타내며, null 값을 허용하지 않음.
 * - updatedAt: 경비 정보가 마지막으로 수정된 시각을 나타내며, null 값을 허용하지 않음.
 * - transportation: 경비가 특정 교통수단(Transportation)과 관련된 경우, 해당 정보를 참조함.
 * - accommodation: 경비가 특정 숙박(Accommodation)과 관련된 경우, 해당 정보를 참조함.
 */
@Entity
@Table(name = "expense")
@Data
public class Expense {

    // 경비 정보의 기본키. DB에서 자동 증가.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer expenseId;

    // 경비가 속한 여행(Trip)을 나타냄. 여러 경비가 하나의 여행에 속함.
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    // 경비가 특정 일정(Itinerary)에 해당하는 경우 해당 일정 정보가 저장됨.
    // 여행 전체 경비라면 이 필드는 null일 수 있음.
    @ManyToOne
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    // 경비의 분류를 나타내는 필드.
    // 예: "교통", "식사", "숙박" 등. 최대 50자, null 불가.
    @Column(nullable = false, length = 50)
    private String category;

    // 경비 금액을 나타내는 필드.
    // 총 10자리 숫자 중 소수점 이하 2자리까지 저장하며, null 불가.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // 경비에 대한 상세 설명.
    // TEXT 타입으로 저장되어 길이 제한이 없음.
    @Column(columnDefinition = "TEXT")
    private String description;

    // 경비 정보가 생성된 시각을 저장. null 불가.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 경비 정보가 마지막으로 수정된 시각을 저장. null 불가.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 경비가 특정 교통수단(Transportation)과 관련된 경우 해당 정보를 참조.
    // 외래키 역할을 하며, 관련 경비가 없는 경우 null일 수 있음.
    @ManyToOne
    @JoinColumn(name = "transportation_id")
    private Transportation transportation;

    // 경비가 특정 숙박(Accommodation)과 관련된 경우 해당 정보를 참조.
    // 외래키 역할을 하며, 관련 경비가 없는 경우 null일 수 있음.
    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;
}
