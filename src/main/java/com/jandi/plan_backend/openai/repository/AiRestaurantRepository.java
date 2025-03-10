package com.jandi.plan_backend.openai.repository;

import com.jandi.plan_backend.openai.entity.AiRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiRestaurantRepository extends JpaRepository<AiRestaurant, Long> {
    List<AiRestaurant> findByCityId(Integer cityId);
}
