package com.jandi.plan_backend.image.repository;

import com.jandi.plan_backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Integer> {

    Optional<Image> findByTargetTypeAndTargetId(String targetType, Integer targetId);

    List<Image> findAllByTargetTypeAndTargetId(String targetType, Integer targetId);

    List<Image> findAllByTargetIdLessThan(int targetId);

    /**
     * 여러 targetId에 대한 이미지를 한 번에 조회 (N+1 방지)
     */
    @Query("SELECT i FROM Image i WHERE i.targetType = :targetType AND i.targetId IN :targetIds")
    List<Image> findAllByTargetTypeAndTargetIdIn(
            @Param("targetType") String targetType,
            @Param("targetIds") List<Integer> targetIds);
}