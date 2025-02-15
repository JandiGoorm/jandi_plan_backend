package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.resource.dto.BannerListDTO;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;

    //생성자를 통한 의존성 주입
    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /**
     * 전체 배너 목록 조회
     */
    public List<BannerListDTO> getAllBanners() {
        //return bannerRepository.findAll();
        return bannerRepository.findAll().stream()
                .map(BannerListDTO::new)
                .collect(Collectors.toList());
    }
}
