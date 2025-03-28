package com.jandi.plan_backend.image;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TempPostIdGeneratorTest {

    @Test
    void testGenerateNegativeId() {
        int tempId = TempPostIdGenerator.generateNegativeId();
        // 수정된 코드에서 항상 음수가 나와야 함
        assertTrue(tempId < 0, "생성된 임시 postId는 음수여야 합니다.");
    }
}
