package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.CommentReported;
import com.jandi.plan_backend.commu.entity.CommunityReported;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostReportRespDTO {
    private final Integer reportId;
    private Integer postId;
    private final UserCommunityDTO user;
    private final LocalDateTime createAt;
    private final String contents;

    public PostReportRespDTO(CommunityReported communityReported) {
        this.reportId = communityReported.getReportId();
        this.postId = communityReported.getCommunity().getPostId();
        this.user = new UserCommunityDTO(communityReported.getUser());
        this.createAt = communityReported.getCreatedAt();
        this.contents = communityReported.getContents();
    }
}
