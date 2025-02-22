package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.Continent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContinentRepository extends JpaRepository<Continent, Long> {
    Optional<Continent> findByName(String continentName);
    List<Continent> findByNameIn(List<String> filters);
}
