package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.tripPlan.trip.repository.TripRepository;
import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.Continent;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.user.repository.ContinentRepository;
import com.jandi.plan_backend.user.repository.CountryRepository;
import com.jandi.plan_backend.user.repository.UserCityPreferenceRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Service
public class ManageTripService {
    private final ImageRepository imageRepository;
    private final TripRepository tripRepository;
    private final ValidationUtil validationUtil;
    private final ContinentRepository continentRepository;
    private final ImageService imageService;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final UserCityPreferenceRepository userCityPreferenceRepository;

    public ManageTripService(
            ImageRepository imageRepository,
            TripRepository tripRepository,
            ValidationUtil validationUtil,
            ContinentRepository continentRepository,
            ImageService imageService,
            CountryRepository countryRepository,
            CityRepository cityRepository,
            UserCityPreferenceRepository userCityPreferenceRepository) {
        this.imageRepository = imageRepository;
        this.tripRepository = tripRepository;
        this.validationUtil = validationUtil;
        this.continentRepository = continentRepository;
        this.imageService = imageService;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.userCityPreferenceRepository = userCityPreferenceRepository;
    }

    /** 여행지 생성 관련 */
    //대륙 생성: 디버깅용, 실제 서비스 중엔 대륙이 추가될 것 같지 않음!
    public ContinentRespDTO createNewContinent(String userEmail, String continentName, MultipartFile file) {
        // 유저 & 권한 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 중복 검증
        if (continentRepository.findByName(continentName).isPresent()) {
            throw new BadRequestExceptionMessage("이미 존재하는 대륙입니다.");
        }

        // 엔티티 생성
        Continent continent = new Continent();
        continent.setName(continentName);
        continentRepository.save(continent);

        // 이미지 업로드 (Image 테이블에 저장)
        // targetType="continent", targetId=continentId
        imageService.uploadImage(file, userEmail, continent.getContinentId(), "continent");

        return new ContinentRespDTO(continent);
    }

    //국가 생성
    public CountryRespDTO createNewCountry(
            String userEmail, String continentName, String countryName) {
        //유저 검증
        User user = validationUtil.validateUserExists(userEmail);

        //대륙 검증
        Continent continent = validationUtil.validateContinentExists(continentName);

        //국가 검증: 이미 존재한다면 추가 생성하지 않음
        if (countryRepository.findByName(countryName).isPresent()) {
            throw new BadRequestExceptionMessage("이미 존재하는 국가입니다.");
        }

        //국가 생성
        Country newCountry = new Country();
        newCountry.setName(countryName);
        newCountry.setContinent(continent);
        countryRepository.save(newCountry);

        return new CountryRespDTO(newCountry);
    }

    // 도시 생성
    public CityRespDTO createNewCity(String userEmail, String countryName,
                                     String cityName, String description, MultipartFile file,
                                     Double latitude, Double longitude) {
        // 유저 & 권한 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 국가 검증
        Country country = validationUtil.validateCountryExists(countryName);

        // 도시 중복 검증
        if (cityRepository.findByName(cityName).isPresent()) {
            throw new BadRequestExceptionMessage("이미 존재하는 도시입니다.");
        }

        // 엔티티 생성
        City newCity = new City();
        newCity.setName(cityName);
        newCity.setCountry(country);
        newCity.setContinent(country.getContinent());
        newCity.setDescription(description);
        newCity.setLatitude(latitude);
        newCity.setLongitude(longitude);
        cityRepository.save(newCity);

        // 이미지 업로드 (Image 테이블에 저장)
        // targetType="city", targetId=cityId
        ImageRespDto imageUrl = imageService.uploadImage(file, userEmail, newCity.getCityId(), "city");

        return new CityRespDTO(newCity, imageUrl.getImageUrl());
    }

    /** 여행지 수정 관련 */
    // 국가 수정
    public CountryRespDTO updateCountry(String userEmail, Integer countryId, String countryName) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 국가 검증
        Country country = validationUtil.validateCountryExists(Long.valueOf(countryId));

        // 입력 검증
        if(countryName == null || countryName.isEmpty())
            throw new BadRequestExceptionMessage("변경할 국가 이름을 입력해주세요");

        country.setName(countryName);
        countryRepository.save(country);
        return new CountryRespDTO(country);
    }

    // 도시 수정
    public CityRespDTO updateCity(String userEmail, Integer cityId, String cityName,
                                  String description, MultipartFile file, Double latitude, Double longitude) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 도시 검증
        City city = validationUtil.validateCityExists(cityId);

        // 이미지 제외한 정보 저장
        if(cityName != null && !cityName.isEmpty()) {city.setName(cityName);}
        if(description != null && !description.isEmpty()) {city.setDescription(description);}
        if(latitude != null && !latitude.isNaN()) {city.setLatitude(latitude);}
        if(longitude != null && !longitude.isNaN()) {city.setLongitude(longitude);}
        cityRepository.save(city);

        // 대체될 이미지가 있다면 기존 이미지 삭제 후 신규 이미지로 치환
        String imageUrl = null;
        if(file != null && !file.isEmpty()) {
            // 기존 이미지 삭제
            imageRepository.findByTargetTypeAndTargetId("city", cityId)
                    .ifPresent(img -> imageService.deleteImage(img.getImageId()));

            // 새 이미지 업로드
            ImageRespDto imageDto = imageService.uploadImage(file, user.getEmail(), cityId, "city");
            imageUrl = imageDto.getImageUrl();

            if(imageUrl == null) {
                throw new BadRequestExceptionMessage("신규 이미지 저장에 실패했습니다.");
            }
        }
        return new CityRespDTO(city, imageUrl);
    }

    /** 여행지 삭제 관련 */
    // 국가 삭제
    public Integer deleteCountry(String userEmail, Integer countryId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 도시 검증
        Country country = validationUtil.validateCountryExists(Long.valueOf(countryId));

        // 삭제할 수 없는 경우 삭제 금지
        List<City> cities = cityRepository.findByCountry_NameIn(Collections.singleton(country.getName()));
        for (City city : cities) { // 하위 도시 중 하나라도 삭제할 수 없는 경우 아예 삭제하지 않음
            if(userCityPreferenceRepository.existsByCity(city)){ // 1. 선호 도시로 등록된 경우
                throw new BadRequestExceptionMessage("해당 국가에 속한 도시인 " + city.getName() + "을/를 선호 도시로 등록한 유저가 있어 삭제가 불가능합니다.");
            }
            else if(tripRepository.existsByCity(city)){ // 2. 여행 계획의 목적지로 등록된 경우
                throw new BadRequestExceptionMessage("해당 국가에 속한 도시인 " + city.getName() + "을/를 목적지로 지정한 여행 계획이 있어 삭제가 불가능합니다.");
            }
        }

        // 삭제 가능하다면 하위 도시를 모두 삭제한 후 국가 삭제
        int deletedCitiesCount = 0;
        for (City city : cities) { // 하위 도시 삭제
            // 이미지 삭제
            imageRepository.findByTargetTypeAndTargetId("city", city.getCityId())
                    .ifPresent(img -> imageService.deleteImage(img.getImageId()));

            // 도시 정보 삭제
            cityRepository.delete(city);
            deletedCitiesCount++;
        }
        countryRepository.delete(country); // 국가 삭제
        return (countryRepository.findById(Long.valueOf(countryId)).isEmpty()) ? deletedCitiesCount : -1;
    }

    // 도시 삭제
    public boolean deleteCity(String userEmail, Integer cityId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);

        // 도시 검증
        City city = validationUtil.validateCityExists(cityId);

        // 삭제할 수 없는 경우 삭제 금지
        if(userCityPreferenceRepository.existsByCity(city)){ // 1. 선호 도시로 등록된 경우
            throw new BadRequestExceptionMessage(city.getName() + "을/를 선호 도시로 등록한 유저가 있어 삭제가 불가능합니다.");
        }
        else if(tripRepository.existsByCity(city)){ // 2. 여행 계획의 목적지로 등록된 경우
            throw new BadRequestExceptionMessage(city.getName() + "을/를 목적지로 지정한 여행 계획이 있어 삭제가 불가능합니다.");
        }

        // 이미지 삭제
        imageRepository.findByTargetTypeAndTargetId("city", cityId)
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));

        // 도시 정보 삭제 및 삭제 결과 반환
        cityRepository.delete(city);
        return cityRepository.findById(cityId).isEmpty();
    }
}
