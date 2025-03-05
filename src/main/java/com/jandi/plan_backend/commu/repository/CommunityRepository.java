package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {
    Optional<Community> findByPostId(Integer postId);
    // 지난 7일간 생성된 게시글의 수 반환
    long countByCreatedAtBetween(LocalDateTime last7Days, LocalDateTime today);
}
