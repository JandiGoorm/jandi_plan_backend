package com.jandi.plan_backend.image.repository;

import com.jandi.plan_backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Image 엔티티에 대한 데이터베이스 접근 인터페이스.
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    // 프로필 사진 조회를 위한 메서드: targetType과 targetId로 검색
    Optional<Image> findByTargetTypeAndTargetId(String targetType, Integer targetId);
}