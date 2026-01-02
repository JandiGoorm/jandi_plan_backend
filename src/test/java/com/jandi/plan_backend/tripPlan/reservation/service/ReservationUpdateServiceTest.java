package com.jandi.plan_backend.tripPlan.reservation.service;

import com.jandi.plan_backend.fixture.ReservationFixture;
import com.jandi.plan_backend.fixture.TripFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationReqDTO;
import com.jandi.plan_backend.tripPlan.reservation.dto.ReservationRespDTO;
import com.jandi.plan_backend.tripPlan.reservation.entitiy.Reservation;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReservationUpdateService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 예약 생성, 수정, 삭제
 * 테스트 기법: 동등분할 (유효/무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class ReservationUpdateServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private TripUtil tripUtil;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationUpdateService reservationUpdateService;

    private User normalUser;
    private User otherUser;
    private City defaultCity;
    private Trip publicTrip;
    private Trip othersTrip;
    private Reservation reservation;
    private ReservationReqDTO reservationReqDTO;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        defaultCity = TripFixture.createDefaultCity();
        publicTrip = TripFixture.createPublicTrip(normalUser, defaultCity);
        othersTrip = TripFixture.createPublicTrip(otherUser, defaultCity);
        reservation = ReservationFixture.createReservation(publicTrip);
        reservationReqDTO = ReservationFixture.createReservationReqDTO();
    }

    // ==================== 예약 생성 테스트 ====================

    @Nested
    @DisplayName("예약 생성")
    class CreateReservationTest {

        @Test
        @DisplayName("[성공] 본인의 여행에 예약 생성")
        void createReservation_ToOwnTrip_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            doNothing().when(tripUtil).isCanEditTrip(publicTrip, normalUser);
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
                Reservation saved = inv.getArgument(0);
                saved.setReservationId(1L);
                return saved;
            });

            // when
            ReservationRespDTO result = reservationUpdateService.createReservation(userEmail, tripId, reservationReqDTO);

            // then
            assertThat(result).isNotNull();
            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        @DisplayName("[실패] 타인의 여행에 예약 생성 시 예외 발생")
        void createReservation_ToOthersTrip_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(othersTrip);
            doThrow(new BadRequestExceptionMessage("해당 작업을 수행할 권한이 없습니다"))
                    .when(tripUtil).isCanEditTrip(othersTrip, normalUser);

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.createReservation(userEmail, tripId, reservationReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("권한");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 여행에 예약 생성 시 예외 발생")
        void createReservation_ToNonExistentTrip_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentTripId = 9999;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateTripExists(nonExistentTripId))
                    .thenThrow(new BadRequestExceptionMessage("존재하지 않는 여행 계획입니다"));

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.createReservation(userEmail, nonExistentTripId, reservationReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("존재하지 않는 여행");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자가 예약 생성 시 예외 발생")
        void createReservation_ByNonExistentUser_ShouldThrowException() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            Integer tripId = 1;

            when(validationUtil.validateUserExists(nonExistentEmail))
                    .thenThrow(new BadRequestExceptionMessage("사용자를 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.createReservation(nonExistentEmail, tripId, reservationReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    // ==================== 예약 수정 테스트 ====================

    @Nested
    @DisplayName("예약 수정")
    class UpdateReservationTest {

        @Test
        @DisplayName("[성공] 본인의 예약 수정")
        void updateReservation_OwnReservation_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer reservationId = 1;
            ReservationReqDTO updateDTO = ReservationFixture.createUpdateReservationReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateReservationExists(reservationId.longValue())).thenReturn(reservation);
            doNothing().when(tripUtil).isCanEditTrip(publicTrip, normalUser);

            // when
            ReservationRespDTO result = reservationUpdateService.updateReservation(userEmail, reservationId, updateDTO);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("[실패] 타인의 예약 수정 시 예외 발생")
        void updateReservation_OthersReservation_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer reservationId = 1;
            Reservation othersReservation = ReservationFixture.createReservation(othersTrip);
            ReservationReqDTO updateDTO = ReservationFixture.createUpdateReservationReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateReservationExists(reservationId.longValue())).thenReturn(othersReservation);
            doThrow(new BadRequestExceptionMessage("해당 작업을 수행할 권한이 없습니다"))
                    .when(tripUtil).isCanEditTrip(othersTrip, normalUser);

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.updateReservation(userEmail, reservationId, updateDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("권한");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 예약 수정 시 예외 발생")
        void updateReservation_NonExistentReservation_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentReservationId = 9999;
            ReservationReqDTO updateDTO = ReservationFixture.createUpdateReservationReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateReservationExists(nonExistentReservationId.longValue()))
                    .thenThrow(new BadRequestExceptionMessage("존재하지 않는 예약 일정입니다"));

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.updateReservation(userEmail, nonExistentReservationId, updateDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("존재하지 않는 예약");
        }
    }

    // ==================== 예약 삭제 테스트 ====================

    @Nested
    @DisplayName("예약 삭제")
    class DeleteReservationTest {

        @Test
        @DisplayName("[성공] 본인의 예약 삭제")
        void deleteReservation_OwnReservation_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer reservationId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateReservationExists(reservationId.longValue())).thenReturn(reservation);
            doNothing().when(tripUtil).isCanEditTrip(publicTrip, normalUser);
            doNothing().when(reservationRepository).delete(reservation);
            when(reservationRepository.existsById(reservationId.longValue())).thenReturn(false);

            // when
            boolean result = reservationUpdateService.deleteReservation(userEmail, reservationId);

            // then
            assertThat(result).isTrue();
            verify(reservationRepository).delete(reservation);
        }

        @Test
        @DisplayName("[실패] 타인의 예약 삭제 시 예외 발생")
        void deleteReservation_OthersReservation_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer reservationId = 1;
            Reservation othersReservation = ReservationFixture.createReservation(othersTrip);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateReservationExists(reservationId.longValue())).thenReturn(othersReservation);
            doThrow(new BadRequestExceptionMessage("해당 작업을 수행할 권한이 없습니다"))
                    .when(tripUtil).isCanEditTrip(othersTrip, normalUser);

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.deleteReservation(userEmail, reservationId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("권한");

            verify(reservationRepository, never()).delete(any(Reservation.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 예약 삭제 시 예외 발생")
        void deleteReservation_NonExistentReservation_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentReservationId = 9999;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateReservationExists(nonExistentReservationId.longValue()))
                    .thenThrow(new BadRequestExceptionMessage("존재하지 않는 예약 일정입니다"));

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.deleteReservation(userEmail, nonExistentReservationId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("존재하지 않는 예약");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자가 예약 삭제 시 예외 발생")
        void deleteReservation_ByNonExistentUser_ShouldThrowException() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            Integer reservationId = 1;

            when(validationUtil.validateUserExists(nonExistentEmail))
                    .thenThrow(new BadRequestExceptionMessage("사용자를 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> reservationUpdateService.deleteReservation(nonExistentEmail, reservationId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }
}
