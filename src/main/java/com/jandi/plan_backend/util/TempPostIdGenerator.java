package com.jandi.plan_backend.util;

public class TempPostIdGenerator {

    /**
     * 음수 int로 임시 postId 생성
     * System.currentTimeMillis() → long → (int) 캐스팅 시 오버플로우 가능
     * 여기서는 단순 예시로 오버플로우 허용
     */
    public static int generateNegativeId() {
        long now = System.currentTimeMillis();  // 예: 1680281000123
        return (int)(-now);                    // 음수 int
    }
}
