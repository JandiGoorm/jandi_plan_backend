package com.jandi.plan_backend.image;

public class TempPostIdGenerator {

    /**
     * 음수 int로 임시 postId 생성
     */
    public static int generateNegativeId() {
        long now = System.currentTimeMillis();
        // int 범위를 초과하지 않도록, now를 int 최대값으로 나눈 나머지를 구함
        int truncated = (int) (now % Integer.MAX_VALUE);

        // 0일 수도 있으니, 무조건 1 이상이 되도록 처리한 뒤 음수화
        if (truncated == 0) {
            truncated = 1;
        }

        return -truncated;
    }

}
