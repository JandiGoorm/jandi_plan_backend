package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.entity.CommunityLike;
import com.jandi.plan_backend.commu.entity.CommunityLikeId;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, CommunityLikeId> {
    //특정 유저가 특정 게시물을 좋아요했는지 검색
    Optional<CommunityLike> findByCommunityAndUser(Community community, User user);

    Iterable<? extends CommunityLike> findByCommunity(Community community);

    Iterable<? extends CommunityLike> findByUser(User user);
}
