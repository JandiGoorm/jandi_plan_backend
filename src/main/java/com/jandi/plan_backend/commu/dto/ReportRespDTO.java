package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Reported;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
public class ReportRespDTO {
    private final Integer reportId;
    private final Integer postId;
    private final UserCommunityDTO user;
    private final LocalDateTime createAt;
    private final String contents;

    public ReportRespDTO(Reported reported) {
        this.reportId = reported.getReportId();
        this.postId = reported.getCommunity().getPostId();
        this.user = new UserCommunityDTO(reported.getUser());
        this.createAt = reported.getCreatedAt();
        this.contents = reported.getContents();
    }
}
