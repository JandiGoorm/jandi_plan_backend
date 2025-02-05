package com.jandi.plan_backend.entity.user;

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
