package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/manage/util")
public class ManageUtilController {
    private final JwtTokenProvider jwtTokenProvider;
    private final ValidationUtil validationUtil;
    private final CommunityRepository communityRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public ManageUtilController(
            JwtTokenProvider jwtTokenProvider,
            ValidationUtil validationUtil,
            CommunityRepository communityRepository,
            TripRepository tripRepository,
            UserRepository userRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.validationUtil = validationUtil;
        this.communityRepository = communityRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    // 유저 월별 가입자 수 불러오기
    @GetMapping("/month/users")
    public Map<String, Long> getMonthUsers(
            @RequestHeader("Authorization") String token
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Seoul")); // 현재 시간
        LocalDateTime curMonth = today.toLocalDate().withDayOfMonth(1).atStartOfDay(); // 현재 달의 1일 00:00:00
        log.info("Today is {}, curMonth is {}", today, curMonth);

        Map<String, Long> map = new LinkedHashMap<>(); //월별로 정렬(저장된 순 정렬)하기 위해 LinkedHashMap 사용

        for (LocalDateTime i = curMonth.minusMonths(11); i.isBefore(today); i = i.plusMonths(1)) {
            LocalDateTime startOfMonth = i;  // 해당 달의 1일 00:00:00
            LocalDateTime endOfMonth = i.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).minusSeconds(1); // 해당 월의 마지막 날 23:59:59

            // 한달 간의 가입자 수 조회 후 저장
            long userResisterCount = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
            String key = i.getYear() + "년 " + i.getMonthValue() + "월";
            map.put(key, userResisterCount);

            log.info("startOfMonth: {}", startOfMonth);
            log.info("endOfMonth: {}", endOfMonth);
            log.info("{}의 가입자 수: {}", key, userResisterCount);
        }

        return map;
    }

    // 통계 정보 불러오기
    @GetMapping("/all")
    public Map<String, Long> getServiceData(
            @RequestHeader("Authorization") String token // 헤더의 Authorization에서 JWT 토큰 받기
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Seoul")); // 현재 시간까지 포함됨
        LocalDateTime last7Days = today.minusDays(7).toLocalDate().atStartOfDay(); // 7일 전 00:00:00부터
        log.info("Today is {}, last7Days ago is {}", today, last7Days);

        long allCommunityCount = communityRepository.count();
        long allTripCount = tripRepository.count();
        long allUserCount = userRepository.count();
        long last7DaysUserCount = userRepository.countByCreatedAtBetween(last7Days, today);

        return Map.of(
                "allCommunityCount", allCommunityCount,
                "allTripCount", allTripCount,
                "allUserCount", allUserCount,
                "last7DaysUserCount", last7DaysUserCount
        );
    }
}
