package com.jandi.plan_backend.util.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
}