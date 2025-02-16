package com.jandi.plan_backend.storage.repository;

import com.jandi.plan_backend.storage.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Image 엔티티에 대한 데이터베이스 접근 인터페이스.
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
}
