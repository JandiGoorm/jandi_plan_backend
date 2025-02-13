package com.jandi.plan_backend.itinerary.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 목적지 정보 엔티티 클래스.
 * "destination" 테이블과 매핑됨.
 *
 * 필드 설명:
 * - destinationId: 목적지의 고유 식별자(PK). DB에서 자동 증가.
 * - itinerary: 해당 목적지가 속한 일정(Itinerary)과 다대일 관계.
 * - name: 목적지 이름. 최대 255자이며 null 불가.
 * - address: 목적지 주소. 최대 255자로 저장.
 * - latitude: 위도 값. 총 10자리 숫자 중 소수점 이하 7자리까지 저장.
 * - longitude: 경도 값. 총 10자리 숫자 중 소수점 이하 7자리까지 저장.
 * - arrivalTime: 도착 시간. LocalTime 타입으로 저장.
 * - departureTime: 출발 시간. LocalTime 타입으로 저장.
 * - createdAt: 목적지 정보가 생성된 시각. LocalDateTime 타입이며 null 불가.
 */
@Entity
@Table(name = "destination")
@Data
public class Destination {

    // 목적지의 기본키. DB가 자동으로 값 생성.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer destinationId;

    // 해당 목적지가 속한 일정(Itinerary)과의 다대일 관계.
    // 여러 목적지가 하나의 일정에 속할 수 있음.
    @ManyToOne
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    // 목적지 이름. null 값을 허용하지 않으며 최대 길이는 255자임.
    @Column(nullable = false, length = 255)
    private String name;

    // 목적지 주소. 최대 길이는 255자로 제한됨.
    @Column(length = 255)
    private String address;

    // 목적지의 위도 값. BigDecimal 타입으로 정밀하게 저장.
    // precision: 전체 자릿수, scale: 소수점 이하 자릿수 (여기서는 7자리 소수점 이하).
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    // 목적지의 경도 값. BigDecimal 타입으로 정밀하게 저장.
    // precision와 scale은 위도와 동일하게 설정됨.
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // 목적지에 도착하는 시간. LocalTime 타입으로 저장.
    private LocalTime arrivalTime;

    // 목적지에서 출발하는 시간. LocalTime 타입으로 저장.
    private LocalTime departureTime;

    // 목적지 정보가 생성된 시각. null 값을 허용하지 않음.
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
