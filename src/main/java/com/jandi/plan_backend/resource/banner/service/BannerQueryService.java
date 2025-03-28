package com.jandi.plan_backend.resource.banner.service;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.resource.banner.dto.BannerListDTO;
import com.jandi.plan_backend.resource.banner.repository.BannerRepository;
import com.jandi.plan_backend.util.BannerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryService {
    private final BannerUtil bannerUtil;
    private final BannerRepository bannerRepository;
    @Value("${image-prefix}") private String prefix;

    /** 전체 배너 목록 조회 */
    public List<BannerListDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(banner -> {
                    // BannerListDTO 생성 시, imageUrl 주입
                    return new BannerListDTO(banner, bannerUtil.getBannerImage(banner));
                })
                .collect(Collectors.toList());
    }

}
