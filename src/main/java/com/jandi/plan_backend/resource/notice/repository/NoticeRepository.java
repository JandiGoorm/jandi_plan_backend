package com.jandi.plan_backend.resource.notice.repository;

import com.jandi.plan_backend.resource.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    // 공지글이 존재하는지 조회하는 메서드
    Optional<Notice> findByNoticeId(Integer noticeId);
}
