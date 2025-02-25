package com.jandi.plan_backend.user.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserCityPreferenceId implements Serializable {
    private Integer user;
    private Integer city;
}
