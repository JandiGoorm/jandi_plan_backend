package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Reported;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportedRepository extends JpaRepository<Reported, Long> {
    Optional<Reported> findByUser_userIdAndCommunity_postId(Integer userId, Integer postId);
}
