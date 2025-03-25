package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.dto.PostReportRespDTO;
import com.jandi.plan_backend.commu.community.dto.ReportReqDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.entity.CommunityReported;
import com.jandi.plan_backend.commu.community.repository.CommunityReportedRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CommunityReportService {
    private final ValidationUtil validationUtil;
    private final CommunityReportedRepository communityReportedRepository;

    public CommunityReportService(
            ValidationUtil validationUtil,
            CommunityReportedRepository communityReportedRepository
    ) {
        this.validationUtil = validationUtil;
        this.communityReportedRepository = communityReportedRepository;
    }

    /** 게시물 신고 */
    public PostReportRespDTO reportPost(String userEmail, Integer postId, ReportReqDTO reportDTO) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 중복 신고 방지
        if(communityReportedRepository.findByUser_userIdAndCommunity_postId(user.getUserId(), postId).isPresent()){
            throw new BadRequestExceptionMessage("이미 신고한 게시글입니다.");
        }

        // 게시물 신고 생성
        CommunityReported communityReported = new CommunityReported();
        communityReported.setUser(user);
        communityReported.setCommunity(post);
        communityReported.setContents(reportDTO.getContents());
        communityReported.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        communityReportedRepository.save(communityReported);

        return new PostReportRespDTO(communityReported);
    }
}
