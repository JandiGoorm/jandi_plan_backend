package com.jandi.plan_backend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 시간대 처리를 위한 유틸리티 클래스.
 * 한국 표준시(KST)를 기준으로 시간을 처리합니다.
 */
public final class TimeUtil {

    /** 한국 표준시 ZoneId */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private TimeUtil() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * 현재 한국 표준시 시간을 반환합니다.
     * @return 현재 KST LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(KST);
    }
}
