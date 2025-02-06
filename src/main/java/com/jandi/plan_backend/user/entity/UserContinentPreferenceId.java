package com.jandi.plan_backend.user.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * user_continent_preference 테이블 복합키
 */
@Data
public class UserContinentPreferenceId implements Serializable {
    private Integer user;
    private Integer continent;
}
