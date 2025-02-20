package com.jandi.plan_backend.resource.repository;

import com.jandi.plan_backend.resource.entity.Banner;
import com.jandi.plan_backend.resource.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    // 공지글이 존재하는지 조회하는 메서드
    Optional<Notice> findByNoticeId(Integer noticeId);
}
