package com.jandi.plan_backend.commu.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class CommunityReqDTO {

    @NonNull
    private final String title;

    @NonNull
    private final String content;

    @NonNull
    private final String hashtag;
}
