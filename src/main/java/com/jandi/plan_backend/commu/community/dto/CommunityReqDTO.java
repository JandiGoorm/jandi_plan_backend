package com.jandi.plan_backend.commu.community.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class CommunityReqDTO {

    @NonNull
    private final String title;

    @NonNull
    private final String content;

    @NonNull
    private final List<String> hashtag;
}
