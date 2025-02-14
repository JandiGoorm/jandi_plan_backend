package com.jandi.plan_backend.resource.repository;

import com.jandi.plan_backend.resource.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
}
