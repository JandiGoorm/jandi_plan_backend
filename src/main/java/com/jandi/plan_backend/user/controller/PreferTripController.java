package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.service.PreferTripService;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trip")
public class PreferTripController {
    private final PreferTripService preferTripService;

    public PreferTripController(PreferTripService preferTripService) {
        this.preferTripService = preferTripService;
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
}
