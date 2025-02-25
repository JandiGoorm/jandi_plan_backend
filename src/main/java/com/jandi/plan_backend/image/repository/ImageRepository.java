package com.jandi.plan_backend.image.repository;

import com.jandi.plan_backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    // targetType과 targetId로 검색
    Optional<Image> findByTargetTypeAndTargetId(String targetType, Integer targetId);
}