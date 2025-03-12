package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.itinerary.entity.Itinerary;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.itinerary.repository.ReservationRepository;
import com.jandi.plan_backend.trip.dto.*;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripLike;
import com.jandi.plan_backend.trip.repository.TripLikeRepository;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.CityRepository;
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

    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";
    private final Sort sortByCreate = Sort.by(Sort.Direction.DESC, "createdAt");

    public TripService(TripRepository tripRepository,
                       TripLikeRepository tripLikeRepository,
                       ValidationUtil validationUtil,
                       ImageService imageService,
                       CityRepository cityRepository,
                       ItineraryRepository itineraryRepository,
                       ReservationRepository reservationRepository) {
        this.tripRepository = tripRepository;
        this.tripLikeRepository = tripLikeRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.cityRepository = cityRepository;
        this.itineraryRepository = itineraryRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * 내 여행 계획 목록 조회
     */
    public Page<MyTripRespDTO> getAllMyTrips(String userEmail, int page, int size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripRepository.countByUser(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripRepository.findByUser(
                        user,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortByCreate)
                ),
                trip -> {
                    TripRespDTO baseDTO = convertToTripRespDTO(trip);
                    return new MyTripRespDTO(baseDTO, trip.getPrivatePlan());
                }
        );
    }

    /**
     * 좋아요한 여행 계획 목록 조회
     */
    public Page<TripRespDTO> getLikedTrips(String userEmail, int page, int size) {
        User user = validationUtil.validateUserExists(userEmail);
        long totalCount = tripLikeRepository.countByUser(user);

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> tripLikeRepository.findByUser(
                        user,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortByCreate)
                ),
                tripLikeObj -> {
                    TripLike tripLike = (TripLike) tripLikeObj;
                    return convertToTripRespDTO(tripLike.getTrip());
                }
        );
    }

    /**
     * 공개된 여행 계획 목록(또는 로그인 유저/관리자에 따른 목록) 조회
     */
    public Page<TripRespDTO> getAllTrips(String userEmail, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "tripId");

        if (userEmail == null) {
            // 미로그인 시 공개 플랜만
            long totalCount = tripRepository.countByPrivatePlan(false);
            return PaginationService.getPagedData(page, size, totalCount,
                    pageable -> tripRepository.findByPrivatePlan(
                            false,
                            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                    ),
                    this::convertToTripRespDTO
            );
        } else {
            User user = validationUtil.validateUserExists(userEmail);
            if (validationUtil.validateUserIsAdmin(user.getUserId())) {
                // 관리자 -> 전체
                long totalCount = tripRepository.count();
                return PaginationService.getPagedData(page, size, totalCount,
                        pageable -> tripRepository.findAll(
                                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                        ),
                        this::convertToTripRespDTO
                );
            } else {
                // 일반 -> 타인 공개 + 본인 전체
                long totalCount = tripRepository.countByPrivatePlan(false) + tripRepository.countByUser(user);
                return PaginationService.getPagedData(page, size, totalCount,
                        pageable -> tripRepository.findByPrivatePlanOrUser(
                                false, user,
                                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                        ),
                        this::convertToTripRespDTO
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
            if (!trip.getUser().getUserId().equals(currentUser.getUserId())) {
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

        return new TripItemRespDTO(convertToTripRespDTO(trip), isLiked);
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

        return convertToTripRespDTO(trip);
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
        tripLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        tripLikeRepository.save(tripLike);

        trip.setLikeCount(trip.getLikeCount() + 1);
        tripRepository.save(trip);

        return new TripLikeRespDTO(
                new MyTripRespDTO(convertToTripRespDTO(trip), trip.getPrivatePlan()),
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
    public boolean isOwnerOfTrip(String userEmail, int tripId) {
        Trip trip = validationUtil.validateTripExists(tripId); // trip 존재 여부 검증
        User user = validationUtil.validateUserExists(userEmail); // 사용자 검증
        return trip.getUser().getUserId().equals(user.getUserId());
    }

    /**
     * 좋아요 많은 상위 10개 공개 여행 계획
     */
    public List<TripRespDTO> getTop10Trips() {
        return tripRepository.findTop10ByPrivatePlanFalseOrderByLikeCountDesc().stream()
                .map(this::convertToTripRespDTO)
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

        return convertToTripRespDTO(trip);
    }

    /**
     * 검색 (제목, 도시명, 둘 다)
     */
    public Page<TripRespDTO> searchTrips(String category, String keyword, int page, int size) {
        if (keyword == null || keyword.trim().length() < 2) {
            throw new BadRequestExceptionMessage("검색어는 2글자 이상 입력");
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        List<Trip> searchList;
        switch (category.toUpperCase()) {
            case "TITLE" -> searchList = tripRepository.searchByTitleContainingIgnoreCase(lowerKeyword);
            case "CITY" -> searchList = tripRepository.searchByCityNameContainingIgnoreCase(lowerKeyword);
            case "BOTH" -> {
                Set<Trip> set = new HashSet<>();
                set.addAll(tripRepository.searchByTitleContainingIgnoreCase(lowerKeyword));
                set.addAll(tripRepository.searchByCityNameContainingIgnoreCase(lowerKeyword));
                searchList = new ArrayList<>(set);
            }
            default -> throw new BadRequestExceptionMessage("카테고리는 TITLE, CITY, BOTH 중 하나");
        }
        searchList.sort(Comparator.comparing(Trip::getTripId).reversed());
        long totalCount = searchList.size();

        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> {
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), searchList.size());
                    List<Trip> pagedList = searchList.subList(start, end);
                    return new PageImpl<>(pagedList, pageable, totalCount);
                },
                this::convertToTripRespDTO
        );
    }

    /**
     * Trip -> TripRespDTO 변환
     * 프로필 이미지, 도시 이미지, 여행 계획 이미지 URL을 구성
     */
    private TripRespDTO convertToTripRespDTO(Trip trip) {
        // 작성자 프로필 이미지
        String userProfileUrl = imageService.getImageByTarget("userProfile", trip.getUser().getUserId())
                .map(img -> urlPrefix + img.getImageUrl())
                .orElse(null);

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
}
