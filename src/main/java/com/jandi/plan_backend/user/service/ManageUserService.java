package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.commu.dto.UserListDTO;
import com.jandi.plan_backend.commu.entity.*;
import com.jandi.plan_backend.commu.repository.*;
import com.jandi.plan_backend.commu.service.CommentService;
import com.jandi.plan_backend.commu.service.PostService;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.entity.TripLike;
import com.jandi.plan_backend.trip.repository.TripLikeRepository;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.trip.service.TripService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.entity.UserCityPreference;
import com.jandi.plan_backend.user.repository.UserCityPreferenceRepository;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class ManageUserService {
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CommunityRepository communityRepository;
    private final PostService postService;
    private final CommentRepository commentRepository;
    private final CommentService commentService;
    private final TripRepository tripRepository;
    private final TripService tripService;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final UserCityPreferenceRepository userCityPreferenceRepository;
    private final CommunityReportedRepository communityReportedRepository;
    private final CommentReportedRepository commentReportedRepository;
    private final TripLikeRepository tripLikeRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    public ManageUserService(ValidationUtil validationUtil, UserRepository userRepository, UserService userService, CommunityRepository communityRepository, PostService postService, CommentRepository commentRepository, CommentService commentService, TripRepository tripRepository, TripService tripService, ImageService imageService, ImageRepository imageRepository, UserCityPreferenceRepository userCityPreferenceRepository, CommunityReportedRepository communityReportedRepository, CommentReportedRepository commentReportedRepository, TripLikeRepository tripLikeRepository, CommunityLikeRepository communityLikeRepository, CommentLikeRepository commentLikeRepository) {
        this.validationUtil = validationUtil;
        this.userRepository = userRepository;
        this.userService = userService;
        this.communityRepository = communityRepository;
        this.postService = postService;
        this.commentRepository = commentRepository;
        this.commentService = commentService;
        this.tripRepository = tripRepository;
        this.tripService = tripService;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.userCityPreferenceRepository = userCityPreferenceRepository;
        this.communityReportedRepository = communityReportedRepository;
        this.commentReportedRepository = commentReportedRepository;
        this.tripLikeRepository = tripLikeRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    //유저 목록 로드
    public Page<UserListDTO> getAllUsers(String userEmail, int page, int size) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        long totalCount = userRepository.count();
        return PaginationService.getPagedData(page, size, totalCount,
                userRepository::findAll, UserListDTO::new);
    }

    //부적절 유저 목록 로드
    public Page<UserListDTO> getRestrictedUsers(String userEmail, int page, int size) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        long totalCount = userRepository.countByReportedIsTrue();
        return PaginationService.getPagedData(page, size, totalCount,
                userRepository::findByReportedIsTrue, UserListDTO::new);
    }

    //유저 제재하기
    public Boolean permitUser(String userEmail, Integer userId) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        //유저 찾기
        User user = userRepository.findByUserId(userId).orElse(null);
        if(user==null) { //사용자 미존재 시 오류 반환
            throw new BadRequestExceptionMessage("사용자가 존재하지 않습니다");
        }
        log.info("선택된 유저: {}{}", user.getEmail(), user.getReported() ? "(제재)" : "(일반)");

        //유저 제재/제한 해제 후 제한 여부 반환
        user.setReported(!user.getReported());
        userRepository.save(user);
        return user.getReported();
    }

    // 유저 강제 탈퇴
    public Boolean withdrawUser(String userEmail, Integer userId) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        // 유저 찾기
        User user = userRepository.findByUserId(userId).orElse(null);
        if(user==null) { //사용자 미존재 시 오류 반환
            throw new BadRequestExceptionMessage("사용자가 존재하지 않습니다");
        }

        log.info("선택된 유저: {}{}", user.getEmail(), user.getReported() ? "(제재)" : "(일반)");
        return userService.deleteUser(user.getEmail());
    }
}
