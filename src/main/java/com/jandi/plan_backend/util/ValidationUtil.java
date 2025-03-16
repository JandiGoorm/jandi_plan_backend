package com.jandi.plan_backend.util;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.itinerary.entity.Reservation;
import com.jandi.plan_backend.itinerary.repository.ReservationRepository;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.repository.NoticeRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.Continent;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.user.repository.ContinentRepository;
import com.jandi.plan_backend.user.repository.CountryRepository;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 검증(Validation) 로직을 모아둔 유틸 클래스.
 */
@Slf4j
@Component
public class ValidationUtil {
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final BannerRepository bannerRepository;
    private final NoticeRepository noticeRepository;
    private final ContinentRepository continentRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final TripRepository tripRepository;
    private final ReservationRepository reservationRepository;

    public ValidationUtil(UserRepository userRepository,
                          CommunityRepository communityRepository,
                          CommentRepository commentRepository,
                          BannerRepository bannerRepository,
                          NoticeRepository noticeRepository,
                          ContinentRepository continentRepository,
                          CountryRepository countryRepository,
                          CityRepository cityRepository,
                          TripRepository tripRepository,
                          ReservationRepository reservationRepository, ReservationRepository reservationRepository1) {

        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.bannerRepository = bannerRepository;
        this.noticeRepository = noticeRepository;
        this.continentRepository = continentRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.tripRepository = tripRepository;
        this.reservationRepository = reservationRepository1;
    }

    /* ==========================
       1) 사용자 관련 검증
       ========================== */
    public User validateUserExists(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    public User validateUserExists(Integer userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    public void validateUserRestricted(User user) {
        if (user.getReported()) {
            throw new BadRequestExceptionMessage("비정상적인 활동이 반복되어 일부 기능 사용이 제한되었습니다.");
        }
    }

    // 검증용
    public Boolean validateUserIsAdmin(User user) {
        return "ADMIN".equals(user.getRoleEnum().name());
    }

    /* ==========================
       2) 커뮤니티(게시글) 관련 검증
       ========================== */
    public Community validatePostExists(Integer postId) {
        return communityRepository.findByPostId(postId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 게시글입니다."));
    }

    public void validateUserIsAuthorOfPost(User user, Community post) {
        if (!Objects.equals(user.getUserId(), post.getUser().getUserId())) {
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
        }
    }

    /* ==========================
       3) 댓글 관련 검증
       ========================== */
    public Comment validateCommentExists(Integer commentId) {
        // commentRepository.findByCommentId(...) => Optional<Object>
        // -> instanceof 체크 후 캐스팅
        Object commentObj = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 댓글입니다."));

        if (!(commentObj instanceof Comment)) {
            throw new BadRequestExceptionMessage("존재하지 않는 댓글입니다.");
        }
        return (Comment) commentObj;
    }

    public void validateUserIsAuthorOfComment(User user, Comment comment) {
        if (!Objects.equals(user.getUserId(), comment.getUserId())) {
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
        }
    }

    /* ==========================
       4) 배너, 공지 등 기타 검증
       ========================== */
    public Banner validateBannerExists(Integer bannerId) {
        return bannerRepository.findByBannerId(bannerId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 배너입니다."));
    }

    public Notice validateNoticeExists(Integer noticeId) {
        return noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 공지입니다."));
    }

    /* ==========================
       5) 대륙, 국가, 도시 관련 검증
       ========================== */
    public Continent validateContinentExists(String continentName) {
        return continentRepository.findByName(continentName)
                .orElseThrow(() -> new BadRequestExceptionMessage("등록되지 않은 대륙입니다."));
    }

    // 오버라이딩
    public Country validateCountryExists(Long countryId) {
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new BadRequestExceptionMessage("등록되지 않은 국가입니다."));
    }

    public Country validateCountryExists(String countryName) {
        // countryRepository.findByName(...) => 만약 Optional<Object> 라면 아래와 같은 로직 필요
        Object countryObj = countryRepository.findByName(countryName)
                .orElseThrow(() -> new BadRequestExceptionMessage("등록되지 않은 국가입니다."));

        if (!(countryObj instanceof Country)) {
            throw new BadRequestExceptionMessage("등록되지 않은 국가입니다.");
        }
        return (Country) countryObj;
    }

    public City validateCityExists(Integer cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 도시입니다."));
    }

    /* ==========================
       6) Trip(여행) 검증 예시
       ========================== */
    // 만약 TripRepository의 PK가 Long이라면, findById(Long) 필요
    // 파라미터로 Integer를 받되, longValue()로 변환
    public Trip validateTripExists(Integer tripId) {
        Object tripObj = tripRepository.findById(tripId.longValue())
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 여행 계획입니다."));

        if (!(tripObj instanceof Trip)) {
            throw new BadRequestExceptionMessage("존재하지 않는 여행 계획입니다.");
        }
        return (Trip) tripObj;
    }

    public void validateUserIsAuthorOfTrip(User user, Trip trip) {
        if (!Objects.equals(user.getUserId(), trip.getUser().getUserId())) {
            log.info("user.getUserId() = {}", user.getUserId());
            log.info("trip.getUser().getUserId() = {}", trip.getUser().getUserId());
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
        }
    }


    public Reservation validateReservationExists(Long reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 예약 일정입니다."));
    }

    /* ==========================
       7) 기타 유틸 메서드
       ========================== */
    public LocalDate ValidateDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            throw new BadRequestExceptionMessage("날짜 형식에 문제가 있습니다. 다시 한번 확인해주세요");
        }
    }
}
