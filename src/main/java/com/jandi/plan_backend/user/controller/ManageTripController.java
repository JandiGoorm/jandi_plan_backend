package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.entity.Continent;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.service.PreferTripService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/manage/trip")
public class ManageTripController {

    private final PreferTripService preferTripService;

    public ManageTripController(PreferTripService preferTripService) {
        this.preferTripService = preferTripService;}

    /** 업로드 */
    // 여행 대륙 업로드
    @PostMapping("/continents")
    public ResponseEntity<?> uploadContinent(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("continent") String continentName,
            @RequestParam("file") MultipartFile file
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = userDetails.getUsername();
        Continent newCountry = preferTripService.createNewContinent(userEmail, continentName, file);

        return ResponseEntity.ok(newCountry);
    }

    // 여행 국가 업로드
    @PostMapping("/countries")
    public ResponseEntity<?> uploadCountry(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("continent") String continentName,
            @RequestParam("country") String countryName
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = userDetails.getUsername();
        Country newCountry = preferTripService.createNewCountry(userEmail, continentName, countryName);

        return ResponseEntity.ok(newCountry);
    }

    // 여행 도시 업로드
    @PostMapping("/cities")
    public ResponseEntity<?> uploadCity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("country") String countryName,
            @RequestParam("city") String cityName,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = userDetails.getUsername();
        City newCity = preferTripService.createNewCity(
                userEmail, countryName, cityName, description, file
        );

        return ResponseEntity.ok(newCity);
    }
}
