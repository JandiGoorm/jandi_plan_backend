package com.jandi.plan_backend.user.entity;

import lombok.Getter;

@Getter
public enum Role {
    USER(0),
    STAFF(1),
    ADMIN(2);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public static Role fromValue(int value) {
        for (Role role : Role.values()) {
            if (role.getValue() == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("잘못된 role 입력: " + value);
    }
}

