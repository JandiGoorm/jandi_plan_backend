package com.jandi.plan_backend.tripPlan.itinerary.service;

import com.jandi.plan_backend.fixture.TripFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.tripPlan.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.tripPlan.itinerary.entity.Itinerary;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ItineraryQueryService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 일정 목록 조회
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class ItineraryQueryServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private TripUtil tripUtil;

    @Mock
    private PlaceUtil placeUtil;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItineraryQueryService itineraryQueryService;

    private User normalUser;
    private User otherUser;
    private City city;
    private Trip publicTrip;
    private Trip privateTrip;
    private Itinerary itinerary;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        city = TripFixture.createDefaultCity();
        publicTrip = TripFixture.createPublicTrip(normalUser, city);
        privateTrip = TripFixture.createPrivateTrip(normalUser, city);
        itinerary = createTestItinerary(publicTrip);
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

    // ==================== 일정 목록 조회 테스트 ====================

    @Nested
    @DisplayName("일정 목록 조회")
    class GetItinerariesTest {

        @Test
        @DisplayName("[성공] 공개 여행 계획의 일정 목록 조회")
        void getItineraries_WithPublicTrip_ShouldReturnItineraries() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;
            List<Itinerary> itineraries = List.of(itinerary);
            ItineraryRespDTO mockDto = createMockItineraryRespDTO();

            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(tripUtil.isCanViewTrip(publicTrip, normalUser)).thenReturn(true);
            when(itineraryRepository.findByTrip_TripId(tripId)).thenReturn(itineraries);
            when(placeUtil.convertPlaceToDto(anyLong(), any(Itinerary.class))).thenReturn(mockDto);

            // when
            List<ItineraryRespDTO> result = itineraryQueryService.getItineraries(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(itineraryRepository).findByTrip_TripId(tripId);
        }

        @Test
        @DisplayName("[성공] 비로그인 사용자가 공개 여행 계획 일정 조회")
        void getItineraries_WithoutLoginForPublicTrip_ShouldReturnItineraries() {
            // given
            String userEmail = "anonymous@example.com"; // 미인증 사용자도 이메일은 있을 수 있음
            Integer tripId = 1;
            List<Itinerary> itineraries = List.of(itinerary);
            ItineraryRespDTO mockDto = createMockItineraryRespDTO();

            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());
            when(tripUtil.isCanViewTrip(publicTrip, null)).thenReturn(true);
            when(itineraryRepository.findByTrip_TripId(tripId)).thenReturn(itineraries);
            when(placeUtil.convertPlaceToDto(anyLong(), any(Itinerary.class))).thenReturn(mockDto);

            // when
            List<ItineraryRespDTO> result = itineraryQueryService.getItineraries(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("[성공] 일정이 없는 여행 계획 조회 시 빈 목록 반환")
        void getItineraries_WithNoItineraries_ShouldReturnEmptyList() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(tripUtil.isCanViewTrip(publicTrip, normalUser)).thenReturn(true);
            when(itineraryRepository.findByTrip_TripId(tripId)).thenReturn(List.of());

            // when
            List<ItineraryRespDTO> result = itineraryQueryService.getItineraries(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] 비공개 여행 계획을 타인이 조회 시 예외 발생")
        void getItineraries_WithPrivateTripByOtherUser_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateTripExists(tripId)).thenReturn(privateTrip);
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(otherUser));
            when(tripUtil.isCanViewTrip(privateTrip, otherUser)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> itineraryQueryService.getItineraries(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("비공개 여행 계획");

            verify(itineraryRepository, never()).findByTrip_TripId(anyInt());
        }

        @Test
        @DisplayName("[성공] 비공개 여행 계획을 작성자가 조회")
        void getItineraries_WithPrivateTripByOwner_ShouldReturnItineraries() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;
            Itinerary privateItinerary = createTestItinerary(privateTrip);
            List<Itinerary> itineraries = List.of(privateItinerary);
            ItineraryRespDTO mockDto = createMockItineraryRespDTO();

            when(validationUtil.validateTripExists(tripId)).thenReturn(privateTrip);
            when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(normalUser));
            when(tripUtil.isCanViewTrip(privateTrip, normalUser)).thenReturn(true);
            when(itineraryRepository.findByTrip_TripId(tripId)).thenReturn(itineraries);
            when(placeUtil.convertPlaceToDto(anyLong(), any(Itinerary.class))).thenReturn(mockDto);

            // when
            List<ItineraryRespDTO> result = itineraryQueryService.getItineraries(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }
    }

    private ItineraryRespDTO createMockItineraryRespDTO() {
        // Mock DTO 생성 - PlaceUtil.convertPlaceToDto가 반환하는 값
        return mock(ItineraryRespDTO.class);
    }
}
