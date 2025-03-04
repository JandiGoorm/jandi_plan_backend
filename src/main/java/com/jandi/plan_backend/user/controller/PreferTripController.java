package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.dto.PreferCityReqDTO;
import com.jandi.plan_backend.user.service.PreferTripService;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trip")
public class PreferTripController {
    private final PreferTripService preferTripService;
    private final JwtTokenProvider jwtTokenProvider;

    public PreferTripController(PreferTripService preferTripService, JwtTokenProvider jwtTokenProvider) {
        this.preferTripService = preferTripService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 조회 */
    // 여행 대륙 조회: 첫 로그인 시 대륙 선택용
    @GetMapping("/continents")
    public ResponseEntity<List<ContinentRespDTO>> getAllContinents(
            @RequestParam("filter") List<String> filter
    ) {
        List<ContinentRespDTO> allContinents = preferTripService.getAllContinents(filter);
        return ResponseEntity.ok(allContinents);
    }

    // 여행 국가 조회: 첫 로그인 시 국가 선택용
    @GetMapping("/countries")
    public ResponseEntity<List<CountryRespDTO>> getAllCountries(
            @RequestParam(value = "filter", required = false) List<String> filter,
            @RequestParam(value = "category", required = false) String category
    ) {
        List<CountryRespDTO> allCountries = preferTripService.getAllCountries(category, filter);
        return ResponseEntity.ok(allCountries);
    }

    // 여행 도시 조회: 첫 로그인 시 도시 선택용
    @GetMapping("/cities")
    public ResponseEntity<List<CityRespDTO>> getAllCites(
            @RequestParam(value = "filter", required = false) List<String> filter,
            @RequestParam(value = "category", required = false) String category
    ) {
        List<CityRespDTO> allCities = preferTripService.getAllCities(category, filter);
        return ResponseEntity.ok(allCities);
    }

    // 상위 10개 도시 조회: 좋아요순, 조회수순 선택
    @GetMapping("/rank")
    public ResponseEntity<List<CityRespDTO>> getRankedCities(
            @RequestParam(value = "sort", defaultValue = "LIKE") String sort,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ){
        List<CityRespDTO> rankedCities = preferTripService.getRankedCities(sort, size);
        return ResponseEntity.ok(rankedCities);
    }

    @GetMapping("/cities/prefer")
    public ResponseEntity<?> getPreferCities(
            @RequestHeader("Authorization") String token
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        List<CityRespDTO> preferCities = preferTripService.getPreferCities(userEmail);
        return ResponseEntity.ok(preferCities);
    }

    @PostMapping("/cities/prefer")
    public ResponseEntity<?> addPreferCities(
            @RequestBody PreferCityReqDTO reqDTO,
            @RequestHeader("Authorization") String token
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        int successCount = preferTripService.addPreferCities(reqDTO.getCities(), userEmail);
        return ResponseEntity.ok(successCount + "개의 도시가 선호 도시로 등록되었습니다");
    }

    @PatchMapping("/cities/prefer")
    public ResponseEntity<?> updatePreferCities(
            @RequestBody PreferCityReqDTO reqDTO,
            @RequestHeader("Authorization") String token
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        int successCount = preferTripService.updatePreferCities(reqDTO.getCities(), userEmail);
        return ResponseEntity.ok(successCount + "개의 도시가 선호 도시로 등록되었습니다");
    }

    @DeleteMapping("/cities/prefer")
    public ResponseEntity<?> deletePreferCities(
            @RequestBody PreferCityReqDTO reqDTO,
            @RequestHeader("Authorization") String token
    ) {
        // Jwt 토큰으로부터 유저 이메일 추출
        String jwtToken = token.replace("Bearer ", "");
        String userEmail = jwtTokenProvider.getEmail(jwtToken);

        int successCount = preferTripService.deletePreferCities(reqDTO.getCities(), userEmail);
        return ResponseEntity.ok(successCount + "개의 도시가 선호 도시에서 삭제되었습니다");
    }
}
