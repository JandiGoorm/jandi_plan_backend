package com.jandi.plan_backend.user.service;

import com.google.api.client.util.Lists;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.entity.*;
import com.jandi.plan_backend.user.repository.ContinentRepository;
import com.jandi.plan_backend.user.repository.CountryRepository;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.user.repository.UserCityPreferenceRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PreferTripService {
    private final ContinentRepository continentRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final UserCityPreferenceRepository userCityPreferenceRepository;
    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";

    public PreferTripService(
            ContinentRepository continentRepository,
            CountryRepository countryRepository,
            CityRepository cityRepository,
            ValidationUtil validationUtil,
            ImageService imageService,
            UserCityPreferenceRepository userCityPreferenceRepository
    ) {
        this.continentRepository = continentRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.userCityPreferenceRepository = userCityPreferenceRepository;
    }

    /** 조회 및 필터링 관련 */
    // 대륙 필터링 조회
    public List<ContinentRespDTO> getAllContinents(List<String> filter) {
        List<Continent> continents = (filter.isEmpty())
                ? continentRepository.findAll()
                : continentRepository.findByNameIn(filter);

        return continents.stream()
                .map(continent -> {
                    // Image 테이블에서 이미지 조회
                    String continentImageUrl = imageService.getImageByTarget("continent", continent.getContinentId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    // DTO 생성시 imageUrl 주입
                    return new ContinentRespDTO(continent, continentImageUrl);
                })
                .collect(Collectors.toList());
    }

    // 국가 필터링 조회
    public List<CountryRespDTO> getAllCountries(String category, List<String> filter) {
        if(category == null) {
            throw new BadRequestExceptionMessage("카테고리는 필수로 입력되어야 합니다");
        }


        List<Country> countries;
        switch (category) {

            case "ALL": { //전체 조회
                countries = countryRepository.findAll();
                break;
            }
            case "CONTINENT":{ //대륙 필터링
                countries = countryRepository.findByContinent_NameIn(filter);
                break;
            }
            case "COUNTRY":{ //국가 필터링
                countries = countryRepository.findByNameIn(filter);
                break;
            }
            default:
                throw new IllegalStateException("카테고리 입력이 잘못되었습니다: " + category);
        }

        return countries.stream()
                .map(CountryRespDTO::new)
                .collect(Collectors.toList());
    }

    // 도시 필터링 조회 (필터가 없으면 전체, 필터가 있으면 부분 조회 예시)
    public List<CityRespDTO> getAllCities(String category, List<String> filter) {
        if(category == null) {
            throw new BadRequestExceptionMessage("카테고리는 필수로 입력되어야 합니다");
        }

        // 1) DB에서 City 목록 조회
        List<City> cities;
        switch (category) {
            case "ALL": { //전체 조회
                cities = cityRepository.findAll();
                break;
            }
            case "CONTINENT":{ //대륙 필터링
                cities = cityRepository.findByContinent_NameIn(filter);
                break;
            }
            case "COUNTRY":{ //국가 필터링
                cities = cityRepository.findByCountry_NameIn(filter);
                break;
            }
            case "CITY":{ //도시 필터링
                cities = cityRepository.findByNameIn(filter);
                break;
            }
            default:
                throw new IllegalStateException("카테고리 입력이 잘못되었습니다: " + category);
        }

        // 2) 각각의 City 엔티티에 대해 Image 테이블에서 URL 조회 후 DTO 변환
        return cities.stream()
                .map(city -> {
                    // imageService를 통해 targetType="city", targetId=cityId 로 이미지 조회
                    String cityImageUrl = imageService.getImageByTarget("city", city.getCityId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);

                    // DTO 생성자에 city + cityImageUrl 전달
                    return new CityRespDTO(city, cityImageUrl);
                })
                .collect(Collectors.toList());
    }

    // 도시 정렬 조회 (좋아요 순 / 조회 순으로 N개 조회)
    public List<CityRespDTO> getRankedCities(String sort, Integer size) {
        // 정렬 기준 생성
        Sort standard = switch (sort) {
            case "LIKE" -> //좋아요 많은 순
                    Sort.by(Sort.Direction.DESC, "likeCount");
            case "SEARCH" -> //조회수 많은 순
                    Sort.by(Sort.Direction.DESC, "searchCount");
            default -> throw new IllegalStateException("정렬 기준 입력이 잘못되었습니다: " + sort);
        };

        // 정렬된 리스트 생성
        List<City> rankedCities = cityRepository.findAll(standard);

        // size만큼 잘라내기 : subList에 의한 메모리 누수를 방지하기 위해 따로 리스트 생성
        if(rankedCities.size() < size || size < 0) {
            throw new BadRequestExceptionMessage("size는 1~" + rankedCities.size() + "이내여야 합니다");
        }
        List<City> resultCities = Lists.newArrayList(rankedCities.subList(0, size));

        // 각각의 City 엔티티에 대해 Image 테이블에서 URL 조회 후 DTO 변환
        return resultCities.stream()
                .map(city -> {
                    // imageService를 통해 targetType="city", targetId=cityId 로 이미지 조회
                    String cityImageUrl = imageService.getImageByTarget("city", city.getCityId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);

                    // DTO 생성자에 city + cityImageUrl 전달
                    return new CityRespDTO(city, cityImageUrl);
                })
                .collect(Collectors.toList());
    }

    /** 선호 도시 관련 CRUD */
    // 선호 도시 조회
    public List<CityRespDTO> getPreferCities(String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 선호 도시 찾기
        List<UserCityPreference> preferences = userCityPreferenceRepository.findByUser_UserId(user.getUserId());

        // CityRespDTO 리스트로 만들어 반환
        return preferences.stream()
                .map(pref -> {
                    City curCity = pref.getCity(); //도시
                    String cityImageUrl = //이미지
                            imageService.getImageByTarget("city", curCity.getCityId())
                            .map(img -> urlPrefix + img.getImageUrl())
                            .orElse(null);
                    return new CityRespDTO(curCity, cityImageUrl);
                })
                .toList();
    }

    // 선호 도시 생성
    public int addPreferCities(List<String> cities, String userEmail) {
        // 유저 & 권한 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 선택된 도시들을 선호 도시에 추가
        int successCount = 0;
        for (String cityName : cities) {
            //예외 처리: 존재하지 않는 도시명이라면 건너뜀
            City curCity = cityRepository.findByName(cityName).orElse(null);
            if(curCity == null ){
                log.info("도시 {}은/는 테이블에 없는 도시이므로 건너뜀", cityName);
                continue;
            }

            //예외 처리: 이미 선호 도시로 등록되어 있다면 건너뜀
            Optional<Object> preference = userCityPreferenceRepository.findByCityAndUser(curCity, user);
            if(preference.isPresent()){
                log.info("도시 {}은/는 이미 선호 도시로 등록되어 있으므로 건너뜀", cityName);
                continue;
            }

            UserCityPreference userCityPreference = new UserCityPreference();
            userCityPreference.setCity(curCity);
            userCityPreference.setUser(user);
            userCityPreference.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userCityPreferenceRepository.save(userCityPreference);
            log.info("도시 {}을/를 선호 도시로 등록 성공", cityName);
            successCount++;
        }
        return successCount;
    }

    public int updatePreferCities(List<String> cities, String userEmail) {
        // 기존에 저장된 모든 선호 도시 삭제
        deletePreferCities(new ArrayList<>(), userEmail);

        // 새로운 선호 도시로 저장
        return addPreferCities(cities, userEmail);
    }

    // 선호 도시 부분 혹은 전체 삭제
    public int deletePreferCities(List<String> cities, String userEmail) {
        User user = validationUtil.validateUserExists(userEmail);

        //cities가 빈 객체일 경우 전체 삭제로 취급
        if (cities.isEmpty()) {
            List<UserCityPreference> preferences = userCityPreferenceRepository.findByUser_UserId(user.getUserId());
            cities = preferences.stream()
                    .map(pref -> pref.getCity().getName()) // City에서 도시 이름 추출
                    .collect(Collectors.toList()); // List<String>으로 변환
        }

        // 선택된 도시들을 선호 도시에서 삭제
        int successCount = 0;
        for (String cityName : cities) {
            //예외 처리: 존재하지 않는 도시명이라면 건너뜀
            City curCity = cityRepository.findByName(cityName).orElse(null);
            if(curCity == null ){
                log.info("도시 {}은/는 테이블에 없는 도시이므로 건너뜀", cityName);
                continue;
            }

            //예외 처리: 선호 도시가 아니라면 건너뜀
            Optional<Object> preference = userCityPreferenceRepository.findByCityAndUser(curCity, user);
            if(preference.isEmpty()){
                log.info("도시 {}은/는 이미 선호 도시로 등록되어 있지 않으므로 건너뜀", cityName);
                continue;
            }

            userCityPreferenceRepository.delete((UserCityPreference) preference.get());
            log.info("도시 {}을/를 선호 도시에서 삭제 성공", cityName);
            successCount++;
        }
        return successCount;
    }
}
