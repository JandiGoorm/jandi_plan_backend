package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.CommunityReported;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityReportedRepository extends JpaRepository<CommunityReported, Long> {
    Optional<CommunityReported> findByUser_userIdAndCommunity_postId(Integer userId, Integer postId);
}
