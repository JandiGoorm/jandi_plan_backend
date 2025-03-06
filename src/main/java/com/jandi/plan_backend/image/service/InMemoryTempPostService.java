package com.jandi.plan_backend.image.service;

import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버 메모리에 임시 postId(음수 int) → userId 매핑을 저장
 */
@Service
public class InMemoryTempPostService {

    /**
     * tempIdMap: tempPostId(음수) -> userId
     */
    private final Map<Integer, Integer> tempIdMap = new ConcurrentHashMap<>();

    /**
     * 임시 postId 등록
     */
    public void putTempId(int tempPostId, int userId) {
        tempIdMap.put(tempPostId, userId);
    }

    /**
     * 임시 postId 검증
     */
    public void validateTempId(int tempPostId, int currentUserId) {
        if (!tempIdMap.containsKey(tempPostId)) {
            throw new BadRequestExceptionMessage("유효하지 않은 임시 postId입니다.");
        }
        int owner = tempIdMap.get(tempPostId);
        if (owner != currentUserId) {
            throw new BadRequestExceptionMessage("임시 postId의 소유자가 아닙니다.");
        }
    }

    /**
     * 임시 postId 제거
     */
    public void removeTempId(int tempPostId) {
        tempIdMap.remove(tempPostId);
    }
}
