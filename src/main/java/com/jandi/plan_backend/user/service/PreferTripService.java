package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.image.dto.ImageResponseDto;
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
        List<Continent> continents = (filter.isEmpty()) ?
                continentRepository.findAll() :
                continentRepository.findByNameIn(filter);

        return continents.stream()
                .map(ContinentRespDTO::new)
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

    // 도시 조회
    public List<CityRespDTO> getAllCities(List<String> filter) {
        List<City> cities = (filter.isEmpty()) ?
                cityRepository.findAll() :
                cityRepository.findByNameIn(filter);

        return cities.stream()
                .map(CityRespDTO::new)
                .collect(Collectors.toList());
    }


    /** 생성 관련 */
    //대륙 생성: 디버깅용, 실제 서비스 중엔 대륙이 추가될 것 같지 않음!
    public Continent createNewContinent(
            String userEmail, String continentName, MultipartFile file) {
        //유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);

        //대륙 검증: 이미 존재한다면 추가 생성하지 않음
        if (continentRepository.findByName(continentName).isPresent()) {
            throw new BadRequestExceptionMessage("이미 존재하는 대륙입니다.");
        }
        Continent continent = new Continent();
        continent.setName(continentName);
        continentRepository.save(continent);

        //이미지 업로드 및 반영
        ImageResponseDto imageDTO = imageService.uploadImage(
                file, userEmail, continent.getContinentId(), "continent");
        continent.setImageUrl(imageDTO.getImageUrl());

        continentRepository.save(continent);
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

    // 도시 생성
    public City createNewCity(
            String userEmail, String countryName, String cityName, String description, MultipartFile file) {
        //유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(user);
        log.info("user: {}", user);

        //국가 검증
        Country country = validationUtil.validateCountryExists(countryName);
        log.info("country: {}", country);

        //도시 검증: 이미 존재한다면 추가 생성하지 않음
        if (cityRepository.findByName(cityName).isPresent()) {
            throw new BadRequestExceptionMessage("이미 존재하는 도시입니다.");
        }

        //도시 생성
        City newCity = new City();
        newCity.setName(cityName);
        newCity.setCountry(country);
        newCity.setContinent(country.getContinent());
        newCity.setDescription(description);

        cityRepository.save(newCity);
        log.info("newCity: {}", newCity);

        //이미지 업로드 및 반영
        ImageResponseDto imageDTO = imageService.uploadImage(
                file, userEmail, newCity.getDestinationId(), "majorDestination");
        newCity.setImageUrl(imageDTO.getImageUrl());
        log.info("imageDTO: {}", imageDTO);

        cityRepository.save(newCity);

        return newCity;
    }
}
