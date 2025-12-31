package com.jandi.plan_backend.tripPlan.trip.service;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.tripPlan.trip.dto.*;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.tripPlan.trip.entity.TripLike;
import com.jandi.plan_backend.tripPlan.trip.entity.TripParticipant;
import com.jandi.plan_backend.tripPlan.trip.repository.TripLikeRepository;
import com.jandi.plan_backend.tripPlan.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.tripPlan.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.util.TimeUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 여행 계획 비즈니스 로직
 */
@Service
public class TripService {

    private final TripRepository tripRepository;
    private final TripLikeRepository tripLikeRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final CityRepository cityRepository;
    private final ItineraryRepository itineraryRepository;
    private final ReservationRepository reservationRepository;
    private final TripParticipantRepository tripParticipantRepository;

    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";
    private final Sort sortByCreate = Sort.by(Sort.Direction.DESC, "createdAt");

    public TripService(TripRepository tripRepository,
                       TripLikeRepository tripLikeRepository,
                       ValidationUtil validationUtil,
                       ImageService imageService,
                       CityRepository cityRepository,
                       ItineraryRepository itineraryRepository,
                       ReservationRepository reservationRepository,
                       TripParticipantRepository tripParticipantRepository) {
        this.tripRepository = tripRepository;
        this.tripLikeRepository = tripLikeRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.cityRepository = cityRepository;
        this.itineraryRepository = itineraryRepository;
        this.reservationRepository = reservationRepository;
        this.tripParticipantRepository = tripParticipantRepository;
    }

    /**
     * 내 여행 계획 목록 조회
     */
    public Page<MyTripRespDTO> getAllMyTrips(String userEmail, Integer page, Integer size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripRepository.countByUser(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByUser(
                        user,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortByCreate)
                ),
                trip -> {
                    TripRespDTO baseDTO = convertToPublicTripRespDTO(trip);
                    return new MyTripRespDTO(baseDTO, trip.getPrivatePlan());
                }
        );
    }

    /**
     * 좋아요한 여행 계획 목록 조회
     */
    public Page<TripRespDTO> getLikedTrips(String userEmail, Integer page, Integer size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripLikeRepository.countByUser(user);

        boolean isAdmin = validationUtil.validateUserIsAdmin(user);
        boolean isStaff = validationUtil.validateUserIsStaff(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripLikeRepository.findByUser(
                        user,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortByCreate)
                ),
                tripLikeObj -> {
                    TripLike tripLike = (TripLike) tripLikeObj;
                    Trip trip = tripLike.getTrip();

                    // 1) 만약 여행 계획이 "공개(false)"라면 -> 누구든 공개 DTO
                    if (!trip.getPrivatePlan()) {
                        return convertToPublicTripRespDTO(trip);
                    }

                    // 2) 비공개(true)인 경우 -> (관리자/스태프/동반자/작성자)면 공개 DTO, 아니면 private DTO
                    boolean isParticipant = tripParticipantRepository
                            .findByTrip_TripIdAndParticipant_UserId(trip.getTripId(), user.getUserId()).isPresent();

                    if (isAdmin || isStaff || isParticipant
                            || trip.getUser().getUserId().equals(user.getUserId())) {
                        return convertToPublicTripRespDTO(trip);
                    } else {
                        return convertToPrivateTripRespDTO(trip);
                    }
                }
        );
    }

    /**
     * 공개된 여행 계획 목록(또는 로그인 유저/관리자에 따른 목록) 조회
     */
    public Page<TripRespDTO> getAllTrips(String userEmail, Integer page, Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "tripId");

        if (userEmail == null) {
            // 미로그인 시 공개 플랜만
            long totalCount = tripRepository.countByPrivatePlan(false);
            return PaginationService.getPagedData(page, size, totalCount,
                    pageable -> tripRepository.findByPrivatePlan(
                            false,
                            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                    ),
                    this::convertToPublicTripRespDTO
            );
        } else {
            User user = validationUtil.validateUserExists(userEmail);
            boolean isAdmin = validationUtil.validateUserIsAdmin(user);
            boolean isStaff = validationUtil.validateUserIsStaff(user);
            if (isAdmin || isStaff) {
                // 관리자 or 스텝 -> 전체
                long totalCount = tripRepository.count();
                return PaginationService.getPagedData(page, size, totalCount,
                        pageable -> tripRepository.findAll(
                                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                        ),
                        this::convertToPublicTripRespDTO
                );
            } else {
                // 일반 -> 타인 공개 + 본인 전체
                long totalCount = tripRepository.countVisibleTrips(user);

                return PaginationService.getPagedData(page, size, totalCount,
                        pageable -> tripRepository.findVisibleTrips(
                                user,
                                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                        ),
                        this::convertToPublicTripRespDTO
                );

            }
        }
    }

    /**
     * 여행 계획 단일 조회
     * - 비공개면 작성자 본인만 가능
     */
    public TripItemRespDTO getSpecTrips(String userEmail, Integer tripId) {
        Trip trip = validationUtil.validateTripExists(tripId);

        // 비공개 접근 권한 체크
        if (trip.getPrivatePlan()) {
            if (userEmail == null) {
                throw new BadRequestExceptionMessage("비공개 여행 계획입니다. 로그인 필요");
            }
            User currentUser = validationUtil.validateUserExists(userEmail);

            boolean isMyPlan = trip.getUser().getUserId().equals(currentUser.getUserId());
            Optional<TripParticipant> isFriendsPlan = tripParticipantRepository.findByTrip_TripIdAndParticipant_UserId(tripId, currentUser.getUserId());
            boolean isAdmin = validationUtil.validateUserIsAdmin(currentUser);
            boolean isStaff = validationUtil.validateUserIsStaff(currentUser);
            if (!(isAdmin || isStaff) // 1. 관리자가 아닌 일반 유저는
            && !isMyPlan && isFriendsPlan.isEmpty()) { // 2. 본인 것도 아니면서 친구 것도 아닌 비공개 여행 계획은 접근 불가
                throw new BadRequestExceptionMessage("비공개 여행 계획 접근 불가");
            }
        }

        // 도시 검색 횟수 증가
        City city = trip.getCity();
        city.setSearchCount(city.getSearchCount() + 1);
        cityRepository.save(city);

        // 좋아요 여부
        boolean isLiked = (userEmail != null) &&
                tripLikeRepository.findByTripAndUser_Email(trip, userEmail).isPresent();

        return new TripItemRespDTO(convertToPublicTripRespDTO(trip), isLiked);
    }

    /**
     * 여행 계획 생성
     */
    public TripRespDTO writeTrip(String userEmail,
                                 String title,
                                 String startDate,
                                 String endDate,
                                 String privatePlan,
                                 Integer budget,
                                 Integer cityId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        LocalDate parsedStart = validationUtil.ValidateDate(startDate);
        LocalDate parsedEnd = validationUtil.ValidateDate(endDate);
        if (!("yes".equals(privatePlan) || "no".equals(privatePlan))) {
            throw new BadRequestExceptionMessage("비공개 여부는 yes/no로 요청해야 합니다.");
        }
        boolean isPrivate = "yes".equals(privatePlan);

        City city = validationUtil.validateCityExists(cityId);
        Trip trip = new Trip(user, title, isPrivate, parsedStart, parsedEnd, budget, city);
        tripRepository.save(trip);

        return convertToPublicTripRespDTO(trip);
    }

    /**
     * 좋아요 추가
     */
    public TripLikeRespDTO addLikeTrip(String userEmail, Integer tripId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        Trip trip = validationUtil.validateTripExists(tripId);
        if (!trip.getUser().getUserId().equals(user.getUserId()) && trip.getPrivatePlan()) {
            throw new BadRequestExceptionMessage("비공개 여행 계획");
        }
        if (trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인 여행 계획은 좋아요 불가");
        }
        if (tripLikeRepository.findByTripAndUser(trip, user).isPresent()) {
            throw new BadRequestExceptionMessage("이미 좋아요한 여행 계획");
        }

        TripLike tripLike = new TripLike();
        tripLike.setTrip(trip);
        tripLike.setUser(user);
        tripLike.setCreatedAt(TimeUtil.now());
        tripLikeRepository.save(tripLike);

        trip.setLikeCount(trip.getLikeCount() + 1);
        tripRepository.save(trip);

        return new TripLikeRespDTO(
                new MyTripRespDTO(convertToPublicTripRespDTO(trip), trip.getPrivatePlan()),
                tripLike.getCreatedAt()
        );
    }

    /**
     * 좋아요 해제
     */
    public boolean deleteLikeTrip(String userEmail, Integer tripId) {
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);

        Optional<TripLike> tripLike = tripLikeRepository.findByTripAndUser(trip, user);
        if (tripLike.isEmpty()) {
            throw new BadRequestExceptionMessage("이미 좋아요 해제됨");
        }
        tripLikeRepository.delete(tripLike.get());

        trip.setLikeCount(trip.getLikeCount() - 1);
        tripRepository.save(trip);
        return true;
    }

    /**
     * 특정 tripId가 userEmail의 소유인지 여부 확인
     */
    public boolean isOwnerOfTrip(String userEmail, Integer tripId) {
        Trip trip = validationUtil.validateTripExists(tripId); // trip 존재 여부 검증
        User user = validationUtil.validateUserExists(userEmail); // 사용자 검증
        return trip.getUser().getUserId().equals(user.getUserId());
    }

    /**
     * 좋아요 많은 상위 10개 공개 여행 계획
     */
    public List<TripRespDTO> getTop10Trips() {
        return tripRepository.findTop10ByPrivatePlanFalseOrderByLikeCountDesc().stream()
                .map(this::convertToPublicTripRespDTO)
                .collect(Collectors.toList());
    }

    /**
     * 내 여행 계획 삭제
     */
    public void deleteMyTrip(Integer tripId, String userEmail) {
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);

        if (!trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획만 삭제 가능");
        }

        // 일정, 예약 삭제
        itineraryRepository.deleteAll(itineraryRepository.findByTrip(trip));
        reservationRepository.deleteAll(reservationRepository.findByTrip(trip));

        // 대표 이미지 삭제
        imageService.getImageByTarget("trip", tripId)
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));

        tripRepository.delete(trip);
    }

    /**
     * 여행 계획 기본 정보 수정
     */
    public TripRespDTO updateTripBasicInfo(String userEmail,
                                           Integer tripId,
                                           String title,
                                           String isPrivate) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        Trip trip = validationUtil.validateTripExists(tripId);
        boolean isOwner = trip.getUser().getUserId().equals(user.getUserId());
        boolean isParticipant = trip.getParticipants().stream()
                .anyMatch(tp -> tp.getParticipant().getUserId().equals(user.getUserId()));
        if (!isOwner && !isParticipant) {
            throw new BadRequestExceptionMessage("수정 권한이 없습니다.");
        }

        boolean privatePlan = switch (isPrivate) {
            case "yes" -> true;
            case "no" -> false;
            default -> throw new BadRequestExceptionMessage("비공개 여부는 yes/no");
        };

        trip.setTitle(title);
        trip.setPrivatePlan(privatePlan);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);

        return convertToPublicTripRespDTO(trip);
    }

    // 검색 메서드 시그니처 수정: userEmail을 추가로 받는다.
    public Page<TripRespDTO> searchTrips(String category, String keyword,
                                         Integer page, Integer size,
                                         String userEmail)
    {
        // (1) 검색어 검증
        if (keyword == null || keyword.trim().length() < 2) {
            throw new BadRequestExceptionMessage("검색어는 2글자 이상 입력");
        }
        String lowerKeyword = keyword.trim().toLowerCase();

        // (2) DB에서 keyword로 Trip 목록 조회
        List<Trip> rawList;
        switch (category.toUpperCase()) {
            case "TITLE" -> rawList = tripRepository.searchByTitleContainingIgnoreCase(lowerKeyword);
            case "CITY"  -> rawList = tripRepository.searchByCityNameContainingIgnoreCase(lowerKeyword);
            case "BOTH"  -> {
                Set<Trip> set = new HashSet<>();
                set.addAll(tripRepository.searchByTitleContainingIgnoreCase(lowerKeyword));
                set.addAll(tripRepository.searchByCityNameContainingIgnoreCase(lowerKeyword));
                rawList = new ArrayList<>(set);
            }
            default -> throw new BadRequestExceptionMessage("카테고리는 TITLE, CITY, BOTH 중 하나");
        }

        // (3) 정렬(내림차순)
        rawList.sort(Comparator.comparing(Trip::getTripId).reversed());
        long totalCount = rawList.size();

        // (4) userEmail -> User (null 가능)
        User currentUser = (userEmail == null) ? null
                : validationUtil.validateUserExists(userEmail);

        // (5) 검색된 Trip 중, '접근 권한이 있는' 것만 필터링
        List<Trip> filteredList = rawList.stream()
                .filter(trip -> canViewTrip(trip, currentUser)) // 접근 가능?
                .collect(Collectors.toList());

        // (6) 필터링된 결과를 다시 정렬 (이미 rawList.sort 했으므로 생략 가능)
        // filteredList.sort(Comparator.comparing(Trip::getTripId).reversed());
        long filteredCount = filteredList.size();

        // (7) 페이지네이션
        return PaginationService.getPagedData(
                page, size, filteredCount,
                pageable -> {
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), filteredList.size());
                    List<Trip> subList = filteredList.subList(start, end);
                    return new PageImpl<>(subList, pageable, filteredCount);
                },
                trip -> toAppropriateDTO(trip, currentUser) // Trip -> TripRespDTO 변환
        );
    }

    /**
     * "이 사용자(currentUser)가 이 Trip을 볼 수 있는가?" 판단
     * - 공개( privatePlan=false )면 누구나
     * - 비공개면 (본인 or 동반자 or 관리자/스태프)만 가능
     */
    private boolean canViewTrip(Trip trip, User currentUser) {
        // 1) 공개 여행 계획이면 누구나 가능
        if (!trip.getPrivatePlan()) {
            return true;
        }
        // 2) 비공개인데 로그인 안 됨 -> 접근 불가
        if (currentUser == null) {
            return false;
        }
        // 3) 관리자 or 스태프 -> 접근 가능
        if (validationUtil.validateUserIsAdmin(currentUser)
                || validationUtil.validateUserIsStaff(currentUser)) {
            return true;
        }
        // 4) 작성자 본인 -> 접근 가능
        if (trip.getUser().getUserId().equals(currentUser.getUserId())) {
            return true;
        }
        // 5) 동반자로 등록되어 있으면 접근 가능
        return tripParticipantRepository
                .findByTrip_TripIdAndParticipant_UserId(trip.getTripId(), currentUser.getUserId())
                .isPresent();
    }

    /**
     * "이 사용자(currentUser)가 이 Trip을 '공개 정보'로 볼 수 있는가?"에 따라
     * - public(세부정보) or private(마스킹) DTO를 반환
     *
     * (검색 결과에서 "비공개 + 권한 없음"인 Trip은 이미 제외했으므로,
     *  실제로 여기서 private DTO가 반환될 일은
     *  '좋아요 목록' 등에서 마스킹이 필요한 경우에만 가능)
     */
    private TripRespDTO toAppropriateDTO(Trip trip, User currentUser) {
        // 1) 공개 플랜이면 무조건 public
        if (!trip.getPrivatePlan()) {
            return convertToPublicTripRespDTO(trip);
        }

        // 2) 비공개지만 사용자 없는 경우 -> private
        if (currentUser == null) {
            return convertToPrivateTripRespDTO(trip);
        }

        // 3) 관리자 or 스태프
        if (validationUtil.validateUserIsAdmin(currentUser)
                || validationUtil.validateUserIsStaff(currentUser)) {
            return convertToPublicTripRespDTO(trip);
        }

        // 4) 본인 or 동반자
        boolean isOwner = trip.getUser().getUserId().equals(currentUser.getUserId());
        boolean isParticipant = tripParticipantRepository
                .findByTrip_TripIdAndParticipant_UserId(trip.getTripId(), currentUser.getUserId())
                .isPresent();

        if (isOwner || isParticipant) {
            return convertToPublicTripRespDTO(trip);
        }

        // 5) 그 외 -> private
        return convertToPrivateTripRespDTO(trip);
    }

    /**
     * 공개된 Trip -> TripRespDTO 변환
     * 프로필 이미지, 도시 이미지, 여행 계획 이미지 URL을 구성
     */
    private TripRespDTO convertToPublicTripRespDTO(Trip trip) {
        // 작성자 프로필 이미지
        String userProfileUrl = imageService.getImageByTarget("profile", trip.getUser().getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElseGet(() -> imageService.getPublicUrlByImageId(1)); // <- 기본 이미지 처리 추가

        // 도시 대표 이미지
        String cityImageUrl = imageService.getImageByTarget("city", trip.getCity().getCityId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        // 사용자 지정 여행계획 대표 이미지
        String tripImageUrl = imageService.getImageByTarget("trip", trip.getTripId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        return new TripRespDTO(trip.getUser(), userProfileUrl, trip, cityImageUrl, tripImageUrl);
    }

    /**
     * 비공개된 Trip -> TripRespDTO 변환
     * 프로필 이미지, 도시 이미지, 여행 계획 이미지 URL을 구성
     */
    private TripRespDTO convertToPrivateTripRespDTO(Trip trip) {
        // 도시 대표 이미지
        String cityImageUrl = imageService.getImageByTarget("city", trip.getCity().getCityId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

        return new TripRespDTO(trip, cityImageUrl);
    }
}
