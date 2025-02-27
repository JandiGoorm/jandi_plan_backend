package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Object> findByName(String countryName);
    List<Country> findByNameIn(List<String> filter); //나라 필터링
    List<Country> findByContinent_NameIn(List<String> filter);
}
