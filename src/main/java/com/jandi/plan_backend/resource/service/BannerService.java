package com.jandi.plan_backend.resource.service;

import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.resource.dto.BannerListDTO;
import com.jandi.plan_backend.resource.dto.BannerWritePostDTO;
import com.jandi.plan_backend.resource.dto.BannerWriteRespDTO;
import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.repository.BannerRepository;
import com.jandi.plan_backend.storage.entity.Image;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;
    private final UserRepository userRepository;

    //생성자를 통한 의존성 주입
    public BannerService(BannerRepository bannerRepository, UserRepository userRepository) {
        this.bannerRepository = bannerRepository;
        this.userRepository = userRepository;
    }

    /** 전체 배너 목록 조회*/
    public List<BannerListDTO> getAllBanners() {
        //return bannerRepository.findAll();
        return bannerRepository.findAll().stream()
                .map(BannerListDTO::new)
                .collect(Collectors.toList());
    }

    /** 배너글 작성 */
    public BannerWriteRespDTO writeBanner(BannerWritePostDTO bannerDTO, String email) {
        //유저 검증
        User user = validateUserExists(email);
        validateUserIsAdmin(user);

        //배너글 생성
        Banner banner = new Banner();
        banner.setCreatedAt(LocalDateTime.now());
        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(bannerDTO.getImageUrl());
        banner.setLinkUrl(bannerDTO.getLinkUrl());

        //DB 저장 및 반환
        bannerRepository.save(banner);
        return new BannerWriteRespDTO(banner);
    }

    /** 검증 검사 메서드 */
    // 사용자의 존재 여부 검증
    private User validateUserExists(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    // 유저가 관리자인지 검증
    private void validateUserIsAdmin(User user) {
        if(user.getUserId() != 1)
            throw new BadRequestExceptionMessage("공지사항을 작성할 권한이 없습니다");
    }
}
