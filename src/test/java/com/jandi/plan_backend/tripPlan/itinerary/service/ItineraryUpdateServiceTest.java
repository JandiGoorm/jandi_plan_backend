package com.jandi.plan_backend.tripPlan.itinerary.service;

import com.jandi.plan_backend.fixture.TripFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.PlaceUtil;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ItineraryUpdateService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 일정 생성, 수정, 삭제
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class ItineraryUpdateServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private TripUtil tripUtil;

    @Mock
    private PlaceUtil placeUtil;

    @InjectMocks
    private ItineraryUpdateService itineraryUpdateService;

    private User normalUser;
    private User otherUser;
    private City city;
    private Trip trip;
    private Itinerary itinerary;
    private ItineraryReqDTO validReqDTO;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        city = TripFixture.createDefaultCity();
        trip = TripFixture.createPublicTrip(normalUser, city);
        itinerary = createTestItinerary(trip);
        validReqDTO = createValidItineraryReqDTO();
    }

    private Itinerary createTestItinerary(Trip trip) {
        Itinerary itinerary = new Itinerary();
        itinerary.setItineraryId(1L);
        itinerary.setTrip(trip);
        itinerary.setPlaceId(12345L);
        itinerary.setTitle("테스트 일정");
        itinerary.setDate(LocalDate.now().plusDays(7));
        itinerary.setStartTime(LocalTime.of(10, 0));
        itinerary.setCost(10000);
        return itinerary;
    }

    private ItineraryReqDTO createValidItineraryReqDTO() {
        return new ItineraryReqDTO(
                12345L,
                LocalDate.now().plusDays(7).toString(),
                "10:00",
                "새 일정",
                15000
        );
    }

    // ==================== 일정 생성 테스트 ====================

    @Nested
    @DisplayName("일정 생성")
    class CreateItineraryTest {

        @Test
        @DisplayName("[성공] 유효한 요청으로 일정 생성")
        void createItinerary_WithValidRequest_ShouldCreateItinerary() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;
            ItineraryRespDTO mockRespDTO = mock(ItineraryRespDTO.class);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(trip);
            doNothing().when(tripUtil).isCanEditTrip(trip, normalUser);
            when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(invocation -> {
                Itinerary saved = invocation.getArgument(0);
                saved.setItineraryId(1L);
                return saved;
            });
            when(placeUtil.convertPlaceToDto(anyLong(), any(Itinerary.class))).thenReturn(mockRespDTO);

            // when
            ItineraryRespDTO result = itineraryUpdateService.createItinerary(userEmail, tripId, validReqDTO);

            // then
            assertThat(result).isNotNull();
            verify(itineraryRepository).save(any(Itinerary.class));
        }

        @Test
        @DisplayName("[실패] 타인의 여행 계획에 일정 생성 시 예외 발생")
        void createItinerary_ToOthersTrip_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(trip);
            doThrow(new BadRequestExceptionMessage("수정 권한이 없습니다"))
                    .when(tripUtil).isCanEditTrip(trip, otherUser);

            // when & then
            assertThatThrownBy(() -> itineraryUpdateService.createItinerary(userEmail, tripId, validReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("수정 권한");

            verify(itineraryRepository, never()).save(any(Itinerary.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 여행 계획에 일정 생성 시 예외 발생")
        void createItinerary_ToNonExistentTrip_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentTripId = 9999;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateTripExists(nonExistentTripId))
                    .thenThrow(new BadRequestExceptionMessage("해당 여행 계획을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> itineraryUpdateService.createItinerary(userEmail, nonExistentTripId, validReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("여행 계획을 찾을 수 없습니다");
        }
    }

    // ==================== 일정 수정 테스트 ====================

    @Nested
    @DisplayName("일정 수정")
    class UpdateItineraryTest {

        @Test
        @DisplayName("[성공] 유효한 요청으로 일정 수정")
        void updateItinerary_WithValidRequest_ShouldUpdateItinerary() {
            // given
            String userEmail = normalUser.getEmail();
            Long itineraryId = 1L;
            ItineraryReqDTO updateReqDTO = new ItineraryReqDTO(
                    null,
                    null,
                    "14:00",
                    "수정된 일정",
                    20000
            );
            ItineraryRespDTO mockRespDTO = mock(ItineraryRespDTO.class);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateItineraryExists(itineraryId)).thenReturn(itinerary);
            doNothing().when(tripUtil).isCanEditTrip(trip, normalUser);
            when(placeUtil.convertPlaceToDto(anyLong(), any(Itinerary.class))).thenReturn(mockRespDTO);

            // when
            ItineraryRespDTO result = itineraryUpdateService.updateItinerary(userEmail, itineraryId, updateReqDTO);

            // then
            assertThat(result).isNotNull();
            assertThat(itinerary.getTitle()).isEqualTo("수정된 일정");
            assertThat(itinerary.getCost()).isEqualTo(20000);
        }

        @Test
        @DisplayName("[실패] 타인의 일정 수정 시 예외 발생")
        void updateItinerary_ByNonOwner_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Long itineraryId = 1L;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateItineraryExists(itineraryId)).thenReturn(itinerary);
            doThrow(new BadRequestExceptionMessage("수정 권한이 없습니다"))
                    .when(tripUtil).isCanEditTrip(trip, otherUser);

            // when & then
            assertThatThrownBy(() -> itineraryUpdateService.updateItinerary(userEmail, itineraryId, validReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("수정 권한");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 일정 수정 시 예외 발생")
        void updateItinerary_WithNonExistentItinerary_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Long nonExistentId = 9999L;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateItineraryExists(nonExistentId))
                    .thenThrow(new BadRequestExceptionMessage("해당 일정을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> itineraryUpdateService.updateItinerary(userEmail, nonExistentId, validReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("일정을 찾을 수 없습니다");
        }
    }

    // ==================== 일정 삭제 테스트 ====================

    @Nested
    @DisplayName("일정 삭제")
    class DeleteItineraryTest {

        @Test
        @DisplayName("[성공] 작성자가 일정 삭제")
        void deleteItinerary_ByOwner_ShouldDeleteSuccessfully() {
            // given
            String userEmail = normalUser.getEmail();
            Long itineraryId = 1L;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateItineraryExists(itineraryId)).thenReturn(itinerary);
            doNothing().when(tripUtil).isCanEditTrip(trip, normalUser);
            doNothing().when(itineraryRepository).delete(itinerary);
            when(itineraryRepository.existsById(itineraryId)).thenReturn(false);

            // when
            boolean result = itineraryUpdateService.deleteItinerary(userEmail, itineraryId);

            // then
            assertThat(result).isTrue();
            verify(itineraryRepository).delete(itinerary);
        }

        @Test
        @DisplayName("[실패] 타인의 일정 삭제 시 예외 발생")
        void deleteItinerary_ByNonOwner_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Long itineraryId = 1L;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateItineraryExists(itineraryId)).thenReturn(itinerary);
            doThrow(new BadRequestExceptionMessage("수정 권한이 없습니다"))
                    .when(tripUtil).isCanEditTrip(trip, otherUser);

            // when & then
            assertThatThrownBy(() -> itineraryUpdateService.deleteItinerary(userEmail, itineraryId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("수정 권한");

            verify(itineraryRepository, never()).delete(any(Itinerary.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 일정 삭제 시 예외 발생")
        void deleteItinerary_WithNonExistentItinerary_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Long nonExistentId = 9999L;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateItineraryExists(nonExistentId))
                    .thenThrow(new BadRequestExceptionMessage("해당 일정을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> itineraryUpdateService.deleteItinerary(userEmail, nonExistentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("일정을 찾을 수 없습니다");
        }
    }
}
