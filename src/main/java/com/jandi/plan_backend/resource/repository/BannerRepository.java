package com.jandi.plan_backend.resource.repository;

import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.resource.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    // 배너글이 존재하는지 조회하는 메서드
    Optional<Banner> findByBannerId(Integer bannerId);
}
