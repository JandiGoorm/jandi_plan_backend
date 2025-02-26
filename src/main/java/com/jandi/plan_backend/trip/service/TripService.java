package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.dto.MyTripRespDTO;
import com.jandi.plan_backend.trip.dto.TripLikeRespDTO;
import com.jandi.plan_backend.trip.dto.TripRespDTO;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripLike;
import com.jandi.plan_backend.trip.repository.TripLikeRepository;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final TripLikeRepository tripLikeRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";

    public TripService(TripRepository tripRepository, TripLikeRepository tripLikeRepository,
                       ValidationUtil validationUtil, ImageService imageService) {
        this.tripRepository = tripRepository;
        this.tripLikeRepository = tripLikeRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
    }

    /** 여행 계획 목록 전체 조회 (공개 설정된 것만 조회) */
    public Page<TripRespDTO> getAllTrips(int page, int size) {
        long totalCount = tripRepository.countByPrivatePlan(false);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByPrivatePlan(false, pageable),
                tripObj -> {
                    Trip trip = (Trip) tripObj;
                    String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    return new TripRespDTO(trip.getUser(), userProfileUrl, trip);
                });
    }

    /** 내 여행 계획 목록 전체 조회 (본인 명의의 계획만 조회) */
    public Page<MyTripRespDTO> getAllMyTrips(String userEmail, int page, int size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripRepository.countByUser(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByUser(user, pageable),
                tripObj -> {
                    Trip trip = (Trip) tripObj;
                    String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    return new MyTripRespDTO(trip.getUser(), userProfileUrl, trip);
                });
    }

    /** 좋아요한 여행 계획 목록 조회 */
    public Page<TripRespDTO> getLikedTrips(String userEmail, int page, int size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripLikeRepository.countByUser(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripLikeRepository.findByUser(user, pageable),
                tripLikeObj -> {
                    TripLike tripLike = (TripLike) tripLikeObj;
                    Trip trip = tripLike.getTrip();
                    String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    return new TripRespDTO(trip.getUser(), userProfileUrl, trip);
                });
    }

    /** 개별 여행 계획 조회 */
    public MyTripRespDTO getSpecTrips(String userEmail, Integer tripId) {
        Trip trip = validationUtil.validateTripExists(tripId);
        User user = validationUtil.validateUserExists(userEmail);
        if (!trip.getUser().getUserId().equals(user.getUserId()) && trip.getPrivatePlan()) {
            throw new BadRequestExceptionMessage("접근 권한이 없습니다.");
        }
        Optional<Image> userProfile = imageService.getImageByTarget("userProfile", user.getUserId());
        String userProfileUrl = userProfile.map(Image::getImageUrl).orElse(null);
        return new MyTripRespDTO(user, userProfileUrl, trip);
    }

    /** 여행 계획 생성 (이미지 입력 제거됨) */
    public TripRespDTO writeTrip(String userEmail, String title, String startDate, String endDate,
                                 String privatePlan, Integer budget, Integer cityId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        LocalDate parsedStartDate = validationUtil.ValidateDate(startDate);
        LocalDate parsedEndDate = validationUtil.ValidateDate(endDate);
        if (!(Objects.equals(privatePlan, "yes") || Objects.equals(privatePlan, "no"))) {
            throw new BadRequestExceptionMessage("비공개 여부는 yes/no로 요청되어야 합니다.");
        }
        boolean isPrivate = Objects.equals(privatePlan, "yes");

        // cityId를 사용하여 City 엔티티 조회 (validateCityExists 메서드가 있다고 가정)
        City city = validationUtil.validateCityExists(cityId);

        Trip trip = new Trip(user, title, isPrivate, parsedStartDate, parsedEndDate, budget, city);
        tripRepository.save(trip);

        String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        return new TripRespDTO(user, userProfileUrl, trip);
    }

    /** 좋아요 리스트에 추가 */
    public TripLikeRespDTO addLikeTrip(String userEmail, Integer tripId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Trip trip = validationUtil.validateTripExists(tripId);
        if (!trip.getUser().getUserId().equals(user.getUserId()) && trip.getPrivatePlan()) {
            throw new BadRequestExceptionMessage("접근 권한이 없습니다.");
        }
        if(trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인의 여행계획은 좋아요할 수 없습니다.");
        } else if(tripLikeRepository.findByTripAndUser(trip, user).isPresent()) {
            throw new BadRequestExceptionMessage("이미 좋아요한 여행계획입니다.");
        }

        TripLike tripLike = new TripLike();
        tripLike.setTrip(trip);
        tripLike.setUser(user);
        tripLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        tripLikeRepository.save(tripLike);

        trip.setLikeCount(trip.getLikeCount() + 1);
        tripRepository.save(trip);

        String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);
        return new TripLikeRespDTO(
                new MyTripRespDTO(trip.getUser(), userProfileUrl, trip),
                tripLike.getCreatedAt()
        );
    }

    /** 좋아요 리스트에서 삭제 */
    public boolean deleteLikeTrip(String userEmail, Integer tripId) {
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);
        Optional<TripLike> tripLike = tripLikeRepository.findByTripAndUser(trip, user);
        if(tripLike.isEmpty()) {
            throw new BadRequestExceptionMessage("이미 좋아요 해제되어 있습니다.");
        }
        tripLikeRepository.delete(tripLike.get());
        trip.setLikeCount(trip.getLikeCount() - 1);
        tripRepository.save(trip);
        return true;
    }

    /** 좋아요 수가 높은 순으로 상위 10개의 공개된 여행 계획 조회 */
    public List<TripRespDTO> getTop10Trips() {
        List<Trip> topTrips = tripRepository.findTop10ByPrivatePlanFalseOrderByLikeCountDesc();
        return topTrips.stream()
                .map(trip -> {
                    String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    return new TripRespDTO(trip.getUser(), userProfileUrl, trip);
                })
                .collect(Collectors.toList());
    }

    /** 내 여행 계획 삭제 */
    public void deleteMyTrip(Integer tripId, String userEmail) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Trip trip = validationUtil.validateTripExists(tripId);
        if (!trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획만 삭제할 수 있습니다.");
        }
        imageService.getImageByTarget("trip", tripId)
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));
        tripRepository.delete(trip);
    }

    /** 여행 계획 기본 정보 수정 (제목, 공개/비공개 여부 등) */
    public TripRespDTO updateTripBasicInfo(String userEmail, Integer tripId, String title, String isPrivate) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Trip trip = validationUtil.validateTripExists(tripId);
        if (!Objects.equals(trip.getUser().getUserId(), user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획만 수정할 수 있습니다.");
        }
        boolean privatePlan;
        if (isPrivate.equalsIgnoreCase("yes")) {
            privatePlan = true;
        } else if (isPrivate.equalsIgnoreCase("no")) {
            privatePlan = false;
        } else {
            throw new BadRequestExceptionMessage("비공개 여부는 yes / no 로만 요청해주세요.");
        }
        trip.setTitle(title);
        trip.setPrivatePlan(privatePlan);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);

        String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);
        return new TripRespDTO(trip.getUser(), userProfileUrl, trip);
    }
}
