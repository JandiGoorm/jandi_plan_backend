package com.jandi.plan_backend.image.repository;

import com.jandi.plan_backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Integer> {

    Optional<Image> findByTargetTypeAndTargetId(String targetType, Integer targetId);

    List<Image> findAllByTargetTypeAndTargetId(String targetType, Integer targetId);

    List<Image> findAllByTargetIdLessThan(int targetId);
}