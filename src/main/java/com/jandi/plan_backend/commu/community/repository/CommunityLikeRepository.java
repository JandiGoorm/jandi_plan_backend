package com.jandi.plan_backend.commu.community.repository;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.entity.CommunityLike;
import com.jandi.plan_backend.commu.community.entity.CommunityLikeId;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, CommunityLikeId> {
    //특정 유저가 특정 게시물을 좋아요했는지 검색
    boolean existsByUserAndCommunity(User user, Community community);
    Optional<CommunityLike> findByUserAndCommunity(User user, Community community);

    Iterable<? extends CommunityLike> findByCommunity(Community community);

    Iterable<? extends CommunityLike> findByUser(User user);

}
