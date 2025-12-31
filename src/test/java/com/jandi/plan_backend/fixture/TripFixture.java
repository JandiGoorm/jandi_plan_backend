package com.jandi.plan_backend.fixture;

import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.Continent;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Trip 관련 테스트 데이터 팩토리 클래스
 */
public class TripFixture {

    private static final LocalDateTime NOW = TimeUtil.now();

    /**
     * 기본 여행 계획 생성
     */
    public static Trip createTrip(User user, City city) {
        Trip trip = new Trip();
        trip.setTripId(1);
        trip.setUser(user);
        trip.setTitle("테스트 여행 계획");
        trip.setStartDate(LocalDate.now().plusDays(7));
        trip.setEndDate(LocalDate.now().plusDays(14));
        trip.setPrivatePlan(false);
        trip.setLikeCount(0);
        trip.setBudget(1000000);
        trip.setCity(city);
        trip.setCreatedAt(NOW);
        trip.setUpdatedAt(NOW);
        return trip;
    }

    /**
     * 공개 여행 계획 생성
     */
    public static Trip createPublicTrip(User user, City city) {
        Trip trip = createTrip(user, city);
        trip.setPrivatePlan(false);
        return trip;
    }

    /**
     * 비공개 여행 계획 생성
     */
    public static Trip createPrivateTrip(User user, City city) {
        Trip trip = createTrip(user, city);
        trip.setPrivatePlan(true);
        return trip;
    }

    /**
     * 특정 ID를 가진 여행 계획 생성
     */
    public static Trip createTripWithId(Integer tripId, User user, City city) {
        Trip trip = createTrip(user, city);
        trip.setTripId(tripId);
        trip.setTitle("테스트 여행 " + tripId);
        return trip;
    }

    /**
     * 테스트용 대륙 생성
     */
    public static Continent createContinent() {
        Continent continent = new Continent();
        continent.setContinentId(1);
        continent.setName("아시아");
        return continent;
    }

    /**
     * 테스트용 국가 생성
     */
    public static Country createCountry(Continent continent) {
        Country country = new Country();
        country.setCountryId(1);
        country.setName("대한민국");
        country.setContinent(continent);
        return country;
    }

    /**
     * 테스트용 도시 생성
     */
    public static City createCity(Country country, Continent continent) {
        City city = new City();
        city.setCityId(1);
        city.setName("서울");
        city.setCountry(country);
        city.setContinent(continent);
        city.setDescription("대한민국의 수도");
        city.setLikeCount(0);
        city.setSearchCount(0);
        city.setLatitude(37.5665);
        city.setLongitude(126.9780);
        return city;
    }

    /**
     * 테스트용 도시 (간단 버전)
     */
    public static City createDefaultCity() {
        Continent continent = createContinent();
        Country country = createCountry(continent);
        return createCity(country, continent);
    }

    /**
     * 여러 여행 계획 목록 생성
     */
    public static List<Trip> createTripList(User user, City city, int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> createTripWithId(i, user, city))
                .toList();
    }
}
