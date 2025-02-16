package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {
    Optional<Community> findByPostId(Integer postId);
}
