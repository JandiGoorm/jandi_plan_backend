package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;

    //생성자를 통한 의존성 주입
    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /**전체 배너 목록 조회*/
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }
}
