package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Community;
import lombok.Getter;

/** 신고 게시물을 조회할 때 넘겨줄 dto.
 * 기존 게시물 조회 dto에 신고 횟수를 함께 넘겨준다
 */

@Getter
public class CommunityReportedListDTO extends CommunityListDTO {
    private final Integer reportCount;

    public CommunityReportedListDTO(Community community, Integer reportCount) {
        super(community);
        this.reportCount = reportCount;
    }
}
