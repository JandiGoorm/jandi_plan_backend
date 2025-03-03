package com.jandi.plan_backend.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class PreferCityReqDTO {
    private List<String> cities;
}
