package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.service.TripService;
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
public class preferTripController {
    private final TripService tripService;

    public preferTripController(TripService tripService) {
        this.tripService = tripService;
    }

    /** 조회 */
    // 여행 대륙 조회
    @GetMapping("/continents")
    public ResponseEntity<List<ContinentRespDTO>> getAllContinents(
            @RequestParam("filter") List<String> filter
    ) {
        List<ContinentRespDTO> allContinents = tripService.getAllContinents(filter);
        return ResponseEntity.ok(allContinents);
    }

    // 여행 국가 조회
    @GetMapping("/countries")
    public ResponseEntity<List<CountryRespDTO>> getAllCountries(
            @RequestParam("filter") List<String> filter
    ) {
        List<CountryRespDTO> allCountries = tripService.getAllCountries(filter);
        return ResponseEntity.ok(allCountries);
    }

    // 여행 도시 조회
    @GetMapping("/cities")
    public ResponseEntity<List<CityRespDTO>> getAllCites(
            @RequestParam("filter") List<String> filter
    ) {
        List<CityRespDTO> allCities = tripService.getAllCities(filter);
        return ResponseEntity.ok(allCities);
    }
}
