package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.dto.MyTripRespDTO;
import com.jandi.plan_backend.trip.dto.TripRespDTO;
import com.jandi.plan_backend.trip.dto.UserTripDTO;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;

    public TripService(TripRepository tripRepository, ValidationUtil validationUtil, ImageService imageService) {
        this.tripRepository = tripRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
    }

    /** 여행 계획 목록 전체 조회 */
    public Page<TripRespDTO> getAllTrips(int page, int size) {
        long totalCount = tripRepository.countByPrivatePlan(false); // 공개된 것만 카운트

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByPrivatePlan(false, pageable),
                tripObj -> {
                    Trip trip = (Trip) tripObj;  // 명시적 캐스팅
                    String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                            .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                            .orElse(null);
                    UserTripDTO userTripDTO = new UserTripDTO(trip.getUser().getUserId(), trip.getUser().getUserName(), userProfileUrl);
                    String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                            .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                            .orElse(null);
                    return new TripRespDTO(userTripDTO,
                            trip.getTripId(),
                            trip.getTitle(),
                            trip.getStartDate(),
                            trip.getEndDate(),
                            trip.getDescription(),
                            trip.getLikeCount(),
                            tripImageUrl);
                });
    }

    /** 내 여행 계획 목록 전체 조회 */
    public Page<MyTripRespDTO> getAllMyTrips(String userEmail, int page, int size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripRepository.countByUser(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByUser(user, pageable),
                tripObj -> {
                    Trip trip = (Trip) tripObj;  // 명시적 캐스팅
                    String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                            .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                            .orElse(null);
                    UserTripDTO userTripDTO = new UserTripDTO(user.getUserId(), user.getUserName(), userProfileUrl);
                    String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                            .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                            .orElse(null);
                    return new MyTripRespDTO(userTripDTO,
                            trip.getTripId(),
                            trip.getTitle(),
                            trip.getStartDate(),
                            trip.getEndDate(),
                            trip.getDescription(),
                            trip.getLikeCount(),
                            tripImageUrl,
                            trip.getPrivatePlan());
                });
    }

    /** 여행 계획 생성 */
    public TripRespDTO writeTrip(String userEmail, String title, String description,
                                 String startDate, String endDate, String privatePlan, MultipartFile image) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.ValidateDate(startDate);
        validationUtil.ValidateDate(endDate);
        if (!(Objects.equals(privatePlan, "yes") || Objects.equals(privatePlan, "no"))) {
            throw new BadRequestExceptionMessage("비공개 여부는 yes/no로 요청되어야 합니다.");
        }
        boolean isPrivate = Objects.equals(privatePlan, "yes");

        // 새 여행 계획 생성
        Trip trip = new Trip();
        trip.setTitle(title);
        trip.setDescription(description);
        trip.setStartDate(LocalDate.parse(startDate));
        trip.setEndDate(LocalDate.parse(endDate));
        trip.setUser(user);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        trip.setLikeCount(0);
        trip.setPrivatePlan(isPrivate);
        tripRepository.save(trip);

        // 이미지 저장: targetType "trip"
        ImageRespDto imageDTO = imageService.uploadImage(image, userEmail, trip.getTripId(), "trip");
        String finalImageUrl = imageDTO.getImageUrl();

        // 작성자 프로필 이미지 조회
        String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
        UserTripDTO userTripDTO = new UserTripDTO(user.getUserId(), user.getUserName(), userProfileUrl);

        return new TripRespDTO(userTripDTO,
                trip.getTripId(),
                trip.getTitle(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDescription(),
                trip.getLikeCount(),
                finalImageUrl);
    }
}
