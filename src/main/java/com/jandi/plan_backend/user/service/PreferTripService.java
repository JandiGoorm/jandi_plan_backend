package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.dto.CityRespDTO;
import com.jandi.plan_backend.user.dto.ContinentRespDTO;
import com.jandi.plan_backend.user.dto.CountryRespDTO;
import com.jandi.plan_backend.user.entity.Continent;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.ContinentRepository;
import com.jandi.plan_backend.user.repository.CountryRepository;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PreferTripService {
    private final ContinentRepository continentRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final String urlPrefix = "https://storage.googleapis.com/plan-storage/";

    public PreferTripService(ContinentRepository continentRepository, CountryRepository countryRepository, CityRepository cityRepository, ValidationUtil validationUtil, ImageService imageService) {
        this.continentRepository = continentRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
    }

    /** 조회 관련 */
    // 대륙 조회
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

    // 국가 조회
    public List<CountryRespDTO> getAllCountries(List<String> filter) {
        List<Country> countries = (filter.isEmpty()) ?
                countryRepository.findAll() :
                countryRepository.findByNameIn(filter);

        return countries.stream()
                .map(CountryRespDTO::new)
                .collect(Collectors.toList());
    }

    /** 도시 목록 조회 (필터가 없으면 전체, 필터가 있으면 부분 조회 예시) */
    public List<CityRespDTO> getAllCities(List<String> filter) {
        // 1) DB에서 City 목록 조회
        List<City> cities = (filter.isEmpty())
                ? cityRepository.findAll()
                : cityRepository.findByNameIn(filter);

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

    /** 생성 관련 */
    //대륙 생성: 디버깅용, 실제 서비스 중엔 대륙이 추가될 것 같지 않음!
    public Continent createNewContinent(String userEmail, String continentName, MultipartFile file) {
        // 유저 & 권한 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

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

        return continent;
    }

    //국가 생성
    public Country createNewCountry(
            String userEmail, String continentName, String countryName) {
        //유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

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

        return newCountry;
    }

    public City createNewCity(String userEmail, String countryName,
                              String cityName, String description, MultipartFile file) {
        // 유저 & 권한 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

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
        cityRepository.save(newCity);

        // 이미지 업로드 (Image 테이블에 저장)
        // targetType="city", targetId=cityId
        imageService.uploadImage(file, userEmail, newCity.getCityId(), "city");

        return newCity;
    }

}
