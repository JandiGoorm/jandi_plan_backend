package com.jandi.plan_backend.commu.repository;

import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.entity.CommunityReported;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommunityReportedRepository extends JpaRepository<CommunityReported, Long> {
    Optional<CommunityReported> findByUser_userIdAndCommunity_postId(Integer userId, Integer postId);

    // 신고된 이력이 있는 게시글을 중복없게 postId 별로 그룹화해서 신고 횟수를 계산한 뒤,
    // 신고 횟수로 1차 정렬 후 동일 신고 횟수라면 postId 역순 정렬해서 반환
    @Query("""
    SELECT post.community, COUNT(post) FROM CommunityReported post GROUP BY post.community
    ORDER BY COUNT(post) DESC, post.community.postId DESC
    """)
    Page<Object[]> findReportedCommunitiesWithCount(Pageable pageable);

    List<CommunityReported> findByUser_userId(Integer userId);

    List<CommunityReported> findByCommunity_PostId(Integer communityPostId);
}
