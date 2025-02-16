package com.jandi.plan_backend.resource.repository;

import com.jandi.plan_backend.resource.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {
}
