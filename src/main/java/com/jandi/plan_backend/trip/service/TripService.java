package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.dto.*;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripLike;
import com.jandi.plan_backend.trip.repository.TripLikeRepository;
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

    /** 여행 계획 목록 전체 조회 */
    public Page<TripRespDTO> getAllTrips(int page, int size) {
        long totalCount = tripRepository.countByPrivatePlan(false); // 공개된 것만 카운트

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByPrivatePlan(false, pageable),
                tripObj -> {
                    Trip trip = (Trip) tripObj;  // 명시적 캐스팅

                    String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);

                    return new TripRespDTO(trip.getUser(), userProfileUrl, trip, tripImageUrl);
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
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);

                    return new MyTripRespDTO(trip.getUser(), userProfileUrl, trip, tripImageUrl);
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
                    String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);

                    return new TripRespDTO(trip.getUser(), userProfileUrl, trip, tripImageUrl);
                });
    }

    /** 개별 여행 계획 조회 */
    public MyTripRespDTO getSpecTrips(String userEmail, Integer tripId) {
        //여행 계획 검증
        Trip trip = validationUtil.validateTripExists(tripId);

        //여행 계획에 대해 유저의 접근 권한 검증: 본인의 여행계획이거나, 공개 설정된 타인의 여행계획일때만 조회 가능
        //즉 타인의 비공개 여행 계획은 조회 불가
        User user = validationUtil.validateUserExists(userEmail);
        if (!trip.getUser().getUserId().equals(user.getUserId()) && trip.getPrivatePlan()) {
            throw new BadRequestExceptionMessage("접근 권한이 없습니다.");
        }

        //DTO 생성 및 반환
        Optional<Image> userProfile = imageService.getImageByTarget("userProfile", user.getUserId());
        String userProfileUrl = userProfile.map(Image::getImageUrl).orElse(null);
        Optional<Image> tripImage = imageService.getImageByTarget("trip", trip.getTripId());
        String tripImageUrl = tripImage.map(Image::getImageUrl).orElse(null);
        return new MyTripRespDTO(user, userProfileUrl, trip, tripImageUrl);
    }

    /** 여행 계획 생성 */
    public TripRespDTO writeTrip(String userEmail, String title, String description,
                                 String startDate, String endDate, String privatePlan, MultipartFile image) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        //데이터 검증
        LocalDate parsedStartDate = validationUtil.ValidateDate(startDate);
        LocalDate parsedEndDate = validationUtil.ValidateDate(endDate);
        if (!(Objects.equals(privatePlan, "yes") || Objects.equals(privatePlan, "no"))) {
            throw new BadRequestExceptionMessage("비공개 여부는 yes/no로 요청되어야 합니다.");
        }
        boolean isPrivate = Objects.equals(privatePlan, "yes");

        // 새 여행 계획 생성
        Trip trip = new Trip(user, title, description, isPrivate, parsedStartDate, parsedEndDate);
        tripRepository.save(trip);

        // 이미지 저장: targetType "trip"
        ImageRespDto imageDTO = imageService.uploadImage(image, userEmail, trip.getTripId(), "trip");
        String finalImageUrl = imageDTO.getImageUrl();

        // 작성자 프로필 이미지 조회
        String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        return new TripRespDTO(user, userProfileUrl, trip, finalImageUrl);
    }

    /** 좋아요 리스트에 추가 */
    public TripLikeRespDTO addLikeTrip(String userEmail, Integer tripId) {
        //유저 검증: 미존재, 부적절 유저인 경우 블락처리
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        //접근 가능 여부 검증: 타인의 비공개 여행계획인 경우 블락처리
        Trip trip = validationUtil.validateTripExists(tripId);
        if (!trip.getUser().getUserId().equals(user.getUserId()) && trip.getPrivatePlan()) {
            throw new BadRequestExceptionMessage("접근 권한이 없습니다.");
        }

        //좋아요 가능 여부 검증: 본인의 여행계획인 경우 블락처리
        if(trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인의 여행계획은 좋아요할 수 없습니다.");
        }else if(tripLikeRepository.findByTripAndUser(trip, user).isPresent()) {
            throw new BadRequestExceptionMessage("이미 좋아요한 여행계획입니다.");
        }

        //좋아요 리스트에 추가
        TripLike tripLike = new TripLike();
        tripLike.setTrip(trip);
        tripLike.setUser(user);
        tripLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        tripLikeRepository.save(tripLike);

        //좋아요 수 업데이트
        trip.setLikeCount(trip.getLikeCount() + 1);
        tripRepository.save(trip);

        //DTO 생성 및 반환
        String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);
        String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        MyTripRespDTO myTripRespDTO = new MyTripRespDTO(user, userProfileUrl, trip, tripImageUrl);
        return new TripLikeRespDTO(myTripRespDTO, tripLike.getCreatedAt());
    }

    /** 좋아요 리스트에서 삭제 */
    public boolean deleteLikeTrip(String userEmail, Integer tripId) {
        //유저 및 여행 계획 검증
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);

        //좋아요 검증: 좋아요하지 않은 여행계획일 경우 블락처리
        Optional<TripLike> tripLike = tripLikeRepository.findByTripAndUser(trip, user);
        if(tripLike.isEmpty()) {
            throw new BadRequestExceptionMessage("이미 좋아요 해제되어 있습니다.");
        }

        //좋아요 리스트에서 삭제
        tripLikeRepository.delete(tripLike.get());

        //좋아요 수 업데이트
        trip.setLikeCount(trip.getLikeCount() - 1);
        tripRepository.save(trip);

        return true;
    }

    /**
     * 좋아요 수가 높은 순으로 상위 10개의 공개된 여행 계획을 조회하여 DTO로 반환.
     */
    public List<TripRespDTO> getTop10Trips() {
        // 1) 좋아요 수 기준 상위 10개 Trip 엔티티를 가져옴
        List<Trip> topTrips = tripRepository.findTop10ByPrivatePlanFalseOrderByLikeCountDesc();

        // 2) Trip 엔티티 -> TripRespDTO 변환
        return topTrips.stream()
                .map(trip -> {
                    // 작성자 프로필 이미지 URL
                    String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                            .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                            .orElse(null);

                    // Trip 이미지 URL
                    String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                            .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                            .orElse(null);

                    // UserTripDTO 생성
                    UserTripDTO userTripDTO = new UserTripDTO(
                            trip.getUser().getUserId(),
                            trip.getUser().getUserName(),
                            userProfileUrl
                    );

                    // TripRespDTO 생성
                    return new TripRespDTO(
                            userTripDTO,
                            trip.getTripId(),
                            trip.getTitle(),
                            trip.getStartDate(),
                            trip.getEndDate(),
                            trip.getDescription(),
                            trip.getLikeCount(),
                            tripImageUrl
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 내 여행 계획 삭제 로직.
     * @param tripId   삭제할 여행 계획의 ID
     * @param userEmail 요청을 보낸 사용자 이메일
     */
    public void deleteMyTrip(Integer tripId, String userEmail) {
        // 1. 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 2. 해당 tripId가 존재하는지 검증
        Trip trip = validationUtil.validateTripExists(tripId);

        // 3. 작성자 일치 여부 검증
        if (!trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획만 삭제할 수 있습니다.");
        }

        // 4. trip에 연결된 이미지가 있다면 삭제 (targetType="trip", targetId=tripId)
        imageService.getImageByTarget("trip", tripId)
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));

        // 5. 최종적으로 여행 계획 삭제
        tripRepository.delete(trip);
    }

    /**
     * 여행 계획 기본 정보 수정 (제목, 공개/비공개 등)
     * @param userEmail  수정 요청 사용자 이메일
     * @param tripId     수정할 여행 ID
     * @param title      새 여행 제목
     * @param isPrivate  "yes" / "no"
     * @return 수정된 TripRespDTO
     */
    public TripRespDTO updateTripBasicInfo(String userEmail, Integer tripId, String title, String isPrivate) {
        // 1) 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 2) 여행 계획 검증 (존재 여부, 작성자 본인인지 등)
        Trip trip = validationUtil.validateTripExists(tripId);
        if (!Objects.equals(trip.getUser().getUserId(), user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획만 수정할 수 있습니다.");
        }

        // 3) 공개/비공개 여부 파싱
        boolean privatePlan;
        if (isPrivate.equalsIgnoreCase("yes")) {
            privatePlan = true;
        } else if (isPrivate.equalsIgnoreCase("no")) {
            privatePlan = false;
        } else {
            throw new BadRequestExceptionMessage("비공개 여부는 yes / no 로만 요청해주세요.");
        }

        // 4) 필드 수정
        trip.setTitle(title);
        trip.setPrivatePlan(privatePlan);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);

        // 5) 수정 결과를 DTO로 변환 (프로필/이미지 정보 포함)
        String userProfileUrl = imageService.getImageByTarget("userProfile", user.getUserId())
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);
        String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                .map(img -> "https://storage.googleapis.com/plan-storage/" + img.getImageUrl())
                .orElse(null);

        UserTripDTO userTripDTO = new UserTripDTO(
                user.getUserId(),
                user.getUserName(),
                userProfileUrl
        );

        return new TripRespDTO(
                userTripDTO,
                trip.getTripId(),
                trip.getTitle(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getDescription(),
                trip.getLikeCount(),
                tripImageUrl
        );
    }
}
