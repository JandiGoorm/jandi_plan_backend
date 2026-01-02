package com.jandi.plan_backend.tripPlan.reservation.service;

import com.jandi.plan_backend.fixture.ReservationFixture;
import com.jandi.plan_backend.fixture.TripFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.TripUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ReservationQueryService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 예약 조회
 * 테스트 기법: 동등분할 (유효/무효 입력), 경계값 분석
 */
@ExtendWith(MockitoExtension.class)
class ReservationQueryServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private TripUtil tripUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationQueryService reservationQueryService;

    private User normalUser;
    private User otherUser;
    private City defaultCity;
    private Trip publicTrip;
    private Trip privateTrip;
    private List<Reservation> reservations;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        defaultCity = TripFixture.createDefaultCity();
        publicTrip = TripFixture.createPublicTrip(normalUser, defaultCity);
        privateTrip = TripFixture.createPrivateTrip(otherUser, defaultCity);
        reservations = ReservationFixture.createMixedReservations(publicTrip);
    }

    // ==================== 예약 목록 조회 테스트 ====================

    @Nested
    @DisplayName("예약 목록 조회")
    class GetReservationTest {

        @Test
        @DisplayName("[성공] 본인의 여행 예약 목록 조회")
        void getReservation_OwnTrip_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripUtil.isCanViewTrip(publicTrip, normalUser)).thenReturn(true);
            when(reservationRepository.findByTrip_TripId(tripId)).thenReturn(reservations);

            // when
            Map<String, Object> result = reservationQueryService.getReservation(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).containsKeys("cost", "data");
            verify(reservationRepository).findByTrip_TripId(tripId);
        }

        @Test
        @DisplayName("[성공] 타인의 공개 여행 예약 목록 조회")
        void getReservation_OthersPublicTrip_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;
            Trip othersPublicTrip = TripFixture.createPublicTrip(otherUser, defaultCity);

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(validationUtil.validateTripExists(tripId)).thenReturn(othersPublicTrip);
            when(tripUtil.isCanViewTrip(othersPublicTrip, normalUser)).thenReturn(true);
            when(reservationRepository.findByTrip_TripId(tripId)).thenReturn(reservations);

            // when
            Map<String, Object> result = reservationQueryService.getReservation(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            verify(reservationRepository).findByTrip_TripId(tripId);
        }

        @Test
        @DisplayName("[실패] 타인의 비공개 여행 예약 목록 조회 시 예외 발생")
        void getReservation_OthersPrivateTrip_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(validationUtil.validateTripExists(tripId)).thenReturn(privateTrip);
            when(tripUtil.isCanViewTrip(privateTrip, normalUser)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> reservationQueryService.getReservation(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("비공개 여행 계획");

            verify(reservationRepository, never()).findByTrip_TripId(anyInt());
        }

        @Test
        @DisplayName("[성공] 본인의 비공개 여행 예약 목록 조회")
        void getReservation_OwnPrivateTrip_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;
            Trip myPrivateTrip = TripFixture.createPrivateTrip(normalUser, defaultCity);

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(validationUtil.validateTripExists(tripId)).thenReturn(myPrivateTrip);
            when(tripUtil.isCanViewTrip(myPrivateTrip, normalUser)).thenReturn(true);
            when(reservationRepository.findByTrip_TripId(tripId)).thenReturn(reservations);

            // when
            Map<String, Object> result = reservationQueryService.getReservation(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            verify(reservationRepository).findByTrip_TripId(tripId);
        }

        @Test
        @DisplayName("[성공] 예약이 없는 여행 조회 - 빈 결과 반환")
        void getReservation_NoReservations_ShouldReturnEmpty() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripUtil.isCanViewTrip(publicTrip, normalUser)).thenReturn(true);
            when(reservationRepository.findByTrip_TripId(tripId)).thenReturn(List.of());

            // when
            Map<String, Object> result = reservationQueryService.getReservation(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 여행 예약 조회 시 예외 발생")
        void getReservation_NonExistentTrip_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentTripId = 9999;

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(validationUtil.validateTripExists(nonExistentTripId))
                    .thenThrow(new BadRequestExceptionMessage("존재하지 않는 여행 계획입니다"));

            // when & then
            assertThatThrownBy(() -> reservationQueryService.getReservation(userEmail, nonExistentTripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("존재하지 않는 여행 계획");
        }

        @Test
        @DisplayName("[성공] 미로그인 사용자가 공개 여행 조회")
        void getReservation_AnonymousUserPublicTrip_ShouldSucceed() {
            // given
            String userEmail = "nonexistent@example.com";
            Integer tripId = 1;

            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripUtil.isCanViewTrip(publicTrip, null)).thenReturn(true);
            when(reservationRepository.findByTrip_TripId(tripId)).thenReturn(reservations);

            // when
            Map<String, Object> result = reservationQueryService.getReservation(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
        }
    }
}
