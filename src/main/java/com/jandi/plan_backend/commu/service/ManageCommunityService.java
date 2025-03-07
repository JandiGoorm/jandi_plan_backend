package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.dto.CommunityReportedListDTO;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.entity.CommunityReported;
import com.jandi.plan_backend.commu.repository.CommunityReportedRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManageCommunityService {
    private final ValidationUtil validationUtil;
    private final CommunityReportedRepository communityReportedRepository;
    private final ImageService imageService;

    public ManageCommunityService(
            ValidationUtil validationUtil,
            CommunityReportedRepository communityReportedRepository,
            ImageService imageService
    ) {
        this.validationUtil = validationUtil;
        this.communityReportedRepository = communityReportedRepository;
        this.imageService = imageService;
    }

    public Page<CommunityReportedListDTO> getReportedPosts(String userEmail, int page, int size) {
        //유저 검증
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        long totalCount = communityReportedRepository.count(); // 전체 신고된 게시글 수
        return PaginationService.getPagedData(page, size, totalCount,
                communityReportedRepository::findReportedCommunitiesWithCount,  // 데이터 조회
                ReportedObj -> {
                    Community community = (Community) ReportedObj[0];  // 신고된 게시글
                    Integer reportCount = ((Number) ReportedObj[1]).intValue();  // 신고 횟수
                    return new CommunityReportedListDTO(community, imageService, reportCount);
                }
        );
    }

}
