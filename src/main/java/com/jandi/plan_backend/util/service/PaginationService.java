package com.jandi.plan_backend.util.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PaginationService {

    public static <T, R> Page<R> getPagedData(int page, int size, long totalCount, Function<Pageable, Page<T>> fetchFunction, Function<T, R> mapper) {

        //page 관련 오류 처리
        int totalPages = (int) Math.ceil((double) totalCount / size); //전체 페이지 수
        if (page < 0 || page > totalPages) {
            throw new RuntimeException("잘못된 페이지 번호 요청");
        }

        //페이지네이션
        Pageable pageable = PageRequest.of(page, size);
        Page<T> entityPage = fetchFunction.apply(pageable);

        return entityPage.map(mapper);
    }

    /**
     * 배치 조회를 지원하는 페이지네이션 메서드 (N+1 최적화)
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param totalCount 전체 개수
     * @param fetchFunction 엔티티 조회 함수
     * @param batchMapper 배치 매핑 함수 (엔티티 목록 -> DTO 목록)
     * @return 페이징된 DTO 목록
     */
    public static <T, R> Page<R> getPagedDataBatch(
            int page, int size, long totalCount,
            Function<Pageable, Page<T>> fetchFunction,
            BiFunction<List<T>, Pageable, List<R>> batchMapper) {

        // page 관련 오류 처리
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (page < 0 || page > totalPages) {
            throw new RuntimeException("잘못된 페이지 번호 요청");
        }

        // 페이지네이션
        Pageable pageable = PageRequest.of(page, size);
        Page<T> entityPage = fetchFunction.apply(pageable);

        // 배치 매핑 적용
        List<R> mappedContent = batchMapper.apply(entityPage.getContent(), pageable);

        return new PageImpl<>(mappedContent, pageable, totalCount);
    }
}