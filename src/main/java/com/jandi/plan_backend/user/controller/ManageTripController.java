package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.service.ManageTripService;
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

    private final ManageTripService manageTripService;
    public ManageTripController(ManageTripService manageTripService) {
        this.manageTripService = manageTripService;
    }

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
        ContinentRespDTO newCountry = manageTripService.createNewContinent(userEmail, continentName, file);

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
        CountryRespDTO newCountry = manageTripService.createNewCountry(userEmail, continentName, countryName);

        return ResponseEntity.ok(newCountry);
    }

    // 여행 도시 업로드
    @PostMapping("/cities")
    public ResponseEntity<?> uploadCity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("country") String countryName,
            @RequestParam("city") String cityName,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = userDetails.getUsername();
        CityRespDTO newCity = manageTripService.createNewCity(
                userEmail, countryName, cityName, description, file, latitude, longitude);

        return ResponseEntity.ok(newCity);
    }

    /** 수정 관련 */
    // 여행 국가 수정
    @PatchMapping("/countries/{countryId}")
    public ResponseEntity<?> updateCountry(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "country", required = false) String countryName,
            @PathVariable Integer countryId
    ) {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = userDetails.getUsername();
        CountryRespDTO newCountry = manageTripService.updateCountry(userEmail, countryId, countryName);

        return ResponseEntity.ok(newCountry);
    }

    // 여행 도시 수정
    @PatchMapping("/cities/{cityId}")
    public ResponseEntity<?> updateCity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "city", required = false) String cityName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @PathVariable Integer cityId
    ){
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        String userEmail = userDetails.getUsername();

        CityRespDTO updatedCity = manageTripService.updateCity(
                userEmail, cityId, cityName, description, file, latitude, longitude);

        return ResponseEntity.ok(updatedCity);
    }

    /** 삭제 관련 */
    // 여행 국가 삭제
    @DeleteMapping("/countries/{countryId}")
    public ResponseEntity<?> deleteCountry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer countryId
    ){
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        String userEmail = userDetails.getUsername();

        Integer deletedCities = manageTripService.deleteCountry(userEmail, countryId);
        String respMsg = (deletedCities >= 0) ?
                "국가 하위에 있던 " + deletedCities + "개의 도시와 해당 국가를 삭제했습니다" :
                "알 수 없는 오류가 발생했습니다. 다시 시도해주세요";
        return (deletedCities >= 0) ?
                ResponseEntity.ok(respMsg) :
                ResponseEntity.badRequest().body(respMsg);
    }

    // 여행 도시 삭제
    @DeleteMapping("/cities/{cityId}")
    public ResponseEntity<?> deleteCity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer cityId
    ){
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        String userEmail = userDetails.getUsername();

        boolean isDeleted = manageTripService.deleteCity(userEmail, cityId);

        return (isDeleted) ?
                ResponseEntity.ok("도시가 삭제되었습니다.") :
                ResponseEntity.badRequest().body("알 수 없는 오류가 발생했습니다. 다시 시도해주세요");
    }
}
