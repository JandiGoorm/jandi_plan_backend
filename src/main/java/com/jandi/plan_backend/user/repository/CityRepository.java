package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByName(String name);
    List<City> findByNameIn(List<String> filter); //도시 필터링
    List<City> findByCountry_NameIn(List<String> filter); //국가 필터링
    List<City> findByContinent_NameIn(List<String> filter); //대륙 필터링
}
