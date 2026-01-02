package com.jandi.plan_backend.tripPlan.trip.service;

import com.jandi.plan_backend.fixture.TripFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.dto.*;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.tripPlan.trip.entity.TripLike;
import com.jandi.plan_backend.tripPlan.trip.repository.TripLikeRepository;
import com.jandi.plan_backend.tripPlan.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.tripPlan.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.CityRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TripService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 여행 계획 목록 조회, 상세 조회, 생성, 수정, 삭제, 좋아요
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripLikeRepository tripLikeRepository;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private ImageService imageService;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TripParticipantRepository tripParticipantRepository;

    @InjectMocks
    private TripService tripService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private City city;
    private Trip publicTrip;
    private Trip privateTrip;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        city = TripFixture.createDefaultCity();
        publicTrip = TripFixture.createPublicTrip(normalUser, city);
        privateTrip = TripFixture.createPrivateTrip(normalUser, city);
    }

    // ==================== 여행 계획 목록 조회 테스트 ====================

    @Nested
    @DisplayName("여행 계획 목록 조회")
    class GetAllTripsTest {

        @Test
        @DisplayName("[성공] 비로그인 사용자가 공개 여행 계획 목록 조회")
        void getAllTrips_WithoutLogin_ShouldReturnPublicTripsOnly() {
            // given
            int page = 0;
            int size = 10;
            String userEmail = null;
            List<Trip> publicTrips = TripFixture.createTripList(normalUser, city, 3);

            when(tripRepository.countByPrivatePlan(false)).thenReturn(3L);
            when(tripRepository.findByPrivatePlan(eq(false), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(publicTrips));
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            Page<TripRespDTO> result = tripService.getAllTrips(userEmail, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            verify(tripRepository).countByPrivatePlan(false);
        }

        @Test
        @DisplayName("[성공] 로그인 사용자가 여행 계획 목록 조회 (공개 + 본인 비공개)")
        void getAllTrips_WithLogin_ShouldReturnVisibleTrips() {
            // given
            int page = 0;
            int size = 10;
            String userEmail = normalUser.getEmail();
            List<Trip> trips = List.of(publicTrip, privateTrip);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateUserIsAdmin(normalUser)).thenReturn(false);
            when(validationUtil.validateUserIsStaff(normalUser)).thenReturn(false);
            when(tripRepository.countVisibleTrips(normalUser)).thenReturn(2L);
            when(tripRepository.findVisibleTrips(eq(normalUser), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(trips));
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            Page<TripRespDTO> result = tripService.getAllTrips(userEmail, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
        }
    }

    // ==================== 내 여행 계획 목록 조회 테스트 ====================

    @Nested
    @DisplayName("내 여행 계획 조회")
    class GetMyTripsTest {

        @Test
        @DisplayName("[성공] 로그인 사용자가 내 여행 계획 목록 조회")
        void getAllMyTrips_WithValidUser_ShouldReturnUserTrips() {
            // given
            String userEmail = normalUser.getEmail();
            int page = 0;
            int size = 10;
            List<Trip> myTrips = TripFixture.createTripList(normalUser, city, 2);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(tripRepository.countByUser(normalUser)).thenReturn(2L);
            when(tripRepository.findByUser(eq(normalUser), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(myTrips));
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            Page<MyTripRespDTO> result = tripService.getAllMyTrips(userEmail, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(tripRepository).countByUser(normalUser);
        }
    }

    // ==================== 여행 계획 상세 조회 테스트 ====================

    @Nested
    @DisplayName("여행 계획 상세 조회")
    class GetSpecTripsTest {

        @Test
        @DisplayName("[성공] 공개 여행 계획 상세 조회")
        void getSpecTrips_WithPublicTrip_ShouldReturnTripDetails() {
            // given
            String userEmail = null;
            Integer tripId = 1;

            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(cityRepository.save(any(City.class))).thenReturn(city);
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            TripItemRespDTO result = tripService.getSpecTrips(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTripId()).isEqualTo(tripId);
            assertThat(result.getTitle()).isEqualTo(publicTrip.getTitle());
        }

        @Test
        @DisplayName("[성공] 비공개 여행 계획을 작성자가 조회")
        void getSpecTrips_WithPrivateTripByOwner_ShouldReturnTripDetails() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateTripExists(tripId)).thenReturn(privateTrip);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateUserIsAdmin(normalUser)).thenReturn(false);
            when(validationUtil.validateUserIsStaff(normalUser)).thenReturn(false);
            when(tripParticipantRepository.findByTrip_TripIdAndParticipant_UserId(tripId, normalUser.getUserId()))
                    .thenReturn(Optional.empty());
            when(cityRepository.save(any(City.class))).thenReturn(city);
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");
            when(tripLikeRepository.findByTripAndUser_Email(privateTrip, userEmail)).thenReturn(Optional.empty());

            // when
            TripItemRespDTO result = tripService.getSpecTrips(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPrivatePlan()).isTrue();
        }

        @Test
        @DisplayName("[실패] 비공개 여행 계획을 비로그인 사용자가 조회 시 예외 발생")
        void getSpecTrips_WithPrivateTripWithoutLogin_ShouldThrowException() {
            // given
            String userEmail = null;
            Integer tripId = 1;

            when(validationUtil.validateTripExists(tripId)).thenReturn(privateTrip);

            // when & then
            assertThatThrownBy(() -> tripService.getSpecTrips(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("로그인 필요");
        }

        @Test
        @DisplayName("[실패] 비공개 여행 계획을 타인이 조회 시 예외 발생")
        void getSpecTrips_WithPrivateTripByOtherUser_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateTripExists(tripId)).thenReturn(privateTrip);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateUserIsAdmin(otherUser)).thenReturn(false);
            when(validationUtil.validateUserIsStaff(otherUser)).thenReturn(false);
            when(tripParticipantRepository.findByTrip_TripIdAndParticipant_UserId(tripId, otherUser.getUserId()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tripService.getSpecTrips(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("접근 불가");
        }
    }

    // ==================== 여행 계획 생성 테스트 ====================

    @Nested
    @DisplayName("여행 계획 생성")
    class WriteTripTest {

        @Test
        @DisplayName("[성공] 유효한 요청으로 여행 계획 생성")
        void writeTrip_WithValidRequest_ShouldCreateTrip() {
            // given
            String userEmail = normalUser.getEmail();
            String title = "새로운 여행";
            String startDate = "2026-02-01";
            String endDate = "2026-02-10";
            String privatePlan = "no";
            Integer budget = 500000;
            Integer cityId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.ValidateDate(startDate)).thenReturn(LocalDate.parse(startDate));
            when(validationUtil.ValidateDate(endDate)).thenReturn(LocalDate.parse(endDate));
            when(validationUtil.validateCityExists(cityId)).thenReturn(city);
            when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
                Trip saved = invocation.getArgument(0);
                saved.setTripId(1);
                return saved;
            });
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            TripRespDTO result = tripService.writeTrip(userEmail, title, startDate, endDate, privatePlan, budget, cityId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            verify(tripRepository).save(any(Trip.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 여행 계획 생성 시 예외 발생")
        void writeTrip_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> tripService.writeTrip(userEmail, "title", "2026-02-01", "2026-02-10", "no", 100000, 1))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");

            verify(tripRepository, never()).save(any(Trip.class));
        }

        @Test
        @DisplayName("[실패] 잘못된 비공개 여부 값으로 생성 시 예외 발생")
        void writeTrip_WithInvalidPrivatePlan_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.ValidateDate(anyString())).thenReturn(LocalDate.now());

            // when & then
            assertThatThrownBy(() -> tripService.writeTrip(userEmail, "title", "2026-02-01", "2026-02-10", "invalid", 100000, 1))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("yes/no");
        }
    }

    // ==================== 여행 계획 수정 테스트 ====================

    @Nested
    @DisplayName("여행 계획 수정")
    class UpdateTripTest {

        @Test
        @DisplayName("[성공] 작성자가 여행 계획 기본 정보 수정")
        void updateTripBasicInfo_ByOwner_ShouldUpdateSuccessfully() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;
            String newTitle = "수정된 여행 제목";
            String isPrivate = "yes";
            publicTrip.setParticipants(List.of());

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripRepository.save(any(Trip.class))).thenReturn(publicTrip);
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            TripRespDTO result = tripService.updateTripBasicInfo(userEmail, tripId, newTitle, isPrivate);

            // then
            assertThat(result).isNotNull();
            assertThat(publicTrip.getTitle()).isEqualTo(newTitle);
            assertThat(publicTrip.getPrivatePlan()).isTrue();
            verify(tripRepository).save(publicTrip);
        }

        @Test
        @DisplayName("[실패] 타인의 여행 계획 수정 시 예외 발생")
        void updateTripBasicInfo_ByNonOwner_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;
            publicTrip.setParticipants(List.of());

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            doNothing().when(validationUtil).validateUserRestricted(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);

            // when & then
            assertThatThrownBy(() -> tripService.updateTripBasicInfo(userEmail, tripId, "title", "no"))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("수정 권한");
        }
    }

    // ==================== 여행 계획 삭제 테스트 ====================

    @Nested
    @DisplayName("여행 계획 삭제")
    class DeleteTripTest {

        @Test
        @DisplayName("[성공] 작성자가 여행 계획 삭제")
        void deleteMyTrip_ByOwner_ShouldDeleteSuccessfully() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(itineraryRepository.findByTrip(publicTrip)).thenReturn(List.of());
            when(reservationRepository.findByTrip(publicTrip)).thenReturn(List.of());
            when(imageService.getImageByTarget("trip", tripId)).thenReturn(Optional.empty());

            // when
            tripService.deleteMyTrip(tripId, userEmail);

            // then
            verify(tripRepository).delete(publicTrip);
        }

        @Test
        @DisplayName("[실패] 타인의 여행 계획 삭제 시 예외 발생")
        void deleteMyTrip_ByNonOwner_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);

            // when & then
            assertThatThrownBy(() -> tripService.deleteMyTrip(tripId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("본인이 작성한");

            verify(tripRepository, never()).delete(any(Trip.class));
        }
    }

    // ==================== 여행 좋아요 테스트 ====================

    @Nested
    @DisplayName("여행 좋아요")
    class TripLikeTest {

        @Test
        @DisplayName("[성공] 타인의 공개 여행 계획에 좋아요")
        void addLikeTrip_ToOthersPublicTrip_ShouldSucceed() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            doNothing().when(validationUtil).validateUserRestricted(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripLikeRepository.findByTripAndUser(publicTrip, otherUser)).thenReturn(Optional.empty());
            when(tripLikeRepository.save(any(TripLike.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(tripRepository.save(any(Trip.class))).thenReturn(publicTrip);
            when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
            when(imageService.getPublicUrlByImageId(anyInt())).thenReturn("https://example.com/default.jpg");

            // when
            TripLikeRespDTO result = tripService.addLikeTrip(userEmail, tripId);

            // then
            assertThat(result).isNotNull();
            verify(tripLikeRepository).save(any(TripLike.class));
        }

        @Test
        @DisplayName("[실패] 본인 여행 계획에 좋아요 시 예외 발생")
        void addLikeTrip_ToOwnTrip_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);

            // when & then
            assertThatThrownBy(() -> tripService.addLikeTrip(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("본인 여행");
        }

        @Test
        @DisplayName("[실패] 이미 좋아요한 여행 계획에 중복 좋아요 시 예외 발생")
        void addLikeTrip_AlreadyLiked_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;
            TripLike existingLike = new TripLike();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            doNothing().when(validationUtil).validateUserRestricted(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripLikeRepository.findByTripAndUser(publicTrip, otherUser)).thenReturn(Optional.of(existingLike));

            // when & then
            assertThatThrownBy(() -> tripService.addLikeTrip(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 좋아요");
        }

        @Test
        @DisplayName("[성공] 좋아요 취소")
        void deleteLikeTrip_WithExistingLike_ShouldSucceed() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;
            TripLike existingLike = new TripLike();
            publicTrip.setLikeCount(1);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripLikeRepository.findByTripAndUser(publicTrip, otherUser)).thenReturn(Optional.of(existingLike));
            when(tripRepository.save(any(Trip.class))).thenReturn(publicTrip);

            // when
            boolean result = tripService.deleteLikeTrip(userEmail, tripId);

            // then
            assertThat(result).isTrue();
            verify(tripLikeRepository).delete(existingLike);
            assertThat(publicTrip.getLikeCount()).isZero();
        }

        @Test
        @DisplayName("[실패] 좋아요하지 않은 여행 계획 좋아요 취소 시 예외 발생")
        void deleteLikeTrip_WithoutExistingLike_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer tripId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateTripExists(tripId)).thenReturn(publicTrip);
            when(tripLikeRepository.findByTripAndUser(publicTrip, otherUser)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tripService.deleteLikeTrip(userEmail, tripId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 좋아요 해제");
        }
    }
}
