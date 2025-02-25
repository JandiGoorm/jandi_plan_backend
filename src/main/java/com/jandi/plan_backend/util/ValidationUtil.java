package com.jandi.plan_backend.util;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.entity.Notice;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import com.jandi.plan_backend.resource.repository.NoticeRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.Continent;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.ContinentRepository;
import com.jandi.plan_backend.user.repository.CountryRepository;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

/** 검증 검사 Util*/
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

    public ValidationUtil(
            UserRepository userRepository, CommunityRepository communityRepository,
            CommentRepository commentRepository, BannerRepository bannerRepository,
            NoticeRepository noticeRepository, ContinentRepository continentRepository,
            CountryRepository countryRepository, CityRepository cityRepository,
            TripRepository tripRepository) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.bannerRepository = bannerRepository;
        this.noticeRepository = noticeRepository;
        this.continentRepository = continentRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.tripRepository = tripRepository;
    }

    /** userRepository */
    // 사용자의 존재 여부 검증
    public User validateUserExists(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    public User validateUserExists(Integer userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    // 사용자 활동 제한 여부 검증
    public void validateUserRestricted(User user) {
        if (user.getReported()) {
            throw new BadRequestExceptionMessage("비정상적인 활동이 반복되어 게시글 작성이 제한되었습니다.");
        }
    }

    // 유저가 관리자인지 검증
    public void validateUserIsAdmin(User user) {
        if(user.getUserId() != 1)
            throw new BadRequestExceptionMessage("관리자 권한이 필요한 작업입니다.");
    }

    /** communityRepository 관련 검증 */
    // 게시글의 존재 여부 검증
    public Community validatePostExists(Integer postId) {
        return communityRepository.findByPostId(postId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 게시글입니다."));
    }

    // 게시물의 작성자인지 검증
    public void validateUserIsAuthorOfPost(User user, Community post) {
        if(!Objects.equals(user.getUserId(), post.getUser().getUserId()))
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
    }

    /** commentRepository 관련 검증 */
    // 댓글의 존재 여부 검증
    public Comment validateCommentExists(Integer commentId) {
        return (Comment) commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 댓글입니다."));
    }

    // 댓글의 작성자인지 검증
    public void validateUserIsAuthorOfComment(User user, Comment comment) {
        if(!Objects.equals(user.getUserId(), comment.getUserId()))
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
    }

    /** BannerRepository 관련 검증 */
    //배너의 존재 여부 검증
    public Banner validateBannerExists(Integer bannerId) {
        return (Banner) bannerRepository.findByBannerId(bannerId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 배너입니다."));
    }


    /** NoticeRepository 관련 검증 */
    //공지사항의 존재 여부 검증
    public Notice validateNoticeExists(Integer noticeId) {
        return (Notice) noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 공지입니다."));
    }

    /** Prefer Continent/Country/Destination Repository 관련 검증 */
    //대륙 관련
    public Continent validateContinentExists(String continentName) {
        return (Continent) continentRepository.findByName(continentName)
                .orElseThrow(() -> new BadRequestExceptionMessage("등록되지 않은 대륙입니다."));
    }

    //국가 관련
    public Country validateCountryExists(String countryName) {
        return (Country) countryRepository.findByName(countryName)
                .orElseThrow(() -> new BadRequestExceptionMessage("등록되지 않은 국가입니다."));
    }

    //도시 관련
    public City validateCityExists(String cityName) {
        return (City) cityRepository.findByName(cityName)
                .orElseThrow(() -> new BadRequestExceptionMessage("등록되지 않은 도시입니다."));
    }

    /** TripRepository 관련 검증 */
    //여행 계획의 존재 여부 검증
    public Trip validateTripExists(Integer tripId) {
        return (Trip) tripRepository.findByTripId(tripId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 여행 계획입니다."));
    }

    /** 기타 검증 유틸 */
    //날짜 검증: YYYY-MM-DD인지 검증
    public void ValidateDate(String date){
        try {
            LocalDate.parse(date);
        }catch (Exception e){
            throw new BadRequestExceptionMessage("날짜 형식에 문제가 있습니다. 다시 한번 확인해주세요");
        }
    }
}
