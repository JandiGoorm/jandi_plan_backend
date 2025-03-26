package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.entity.CommunityLike;
import com.jandi.plan_backend.commu.community.repository.CommunityLikeRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
public class CommunityLikeService {
    private final ValidationUtil validationUtil;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityRepository communityRepository;

    public CommunityLikeService(
            ValidationUtil validationUtil,
            CommunityLikeRepository communityLikeRepository,
            CommunityRepository communityRepository) {
        this.validationUtil = validationUtil;
        this.communityLikeRepository = communityLikeRepository;
        this.communityRepository = communityRepository;
    }

    /** 게시물 좋아요 */
    @Transactional
    public void likePost(String userEmail, Integer postId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Community post = validationUtil.validatePostExists(postId);

        if(user.getUserId().equals(post.getUser().getUserId())){
            throw new BadRequestExceptionMessage("본인의 게시글에 좋아요할 수 없습니다.");
        }
        if(communityLikeRepository.findByCommunityAndUser(post, user).isPresent()){
            throw new BadRequestExceptionMessage("이미 좋아요한 게시물입니다.");
        }

        CommunityLike communityLike = new CommunityLike();
        communityLike.setCommunity(post);
        communityLike.setUser(user);
        communityLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        communityLikeRepository.save(communityLike);
        communityRepository.incrementLikeCount(postId);
    }

    /** 게시물 좋아요 취소 */
    @Transactional
    public void deleteLikePost(String userEmail, Integer postId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Community post = validationUtil.validatePostExists(postId);

        Optional<CommunityLike> communityLike = communityLikeRepository.findByCommunityAndUser(post, user);
        if(communityLike.isEmpty()){
            throw new BadRequestExceptionMessage("좋아요한 적 없는 게시물입니다.");
        }
        communityLikeRepository.delete(communityLike.get());
        communityRepository.decrementLikeCount(postId);
    }
}
