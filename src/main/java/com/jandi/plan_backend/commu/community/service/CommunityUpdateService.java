package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.dto.CommunityReqDTO;
import com.jandi.plan_backend.commu.community.dto.CommunityRespDTO;
import com.jandi.plan_backend.commu.community.dto.PostFinalizeReqDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityLikeRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityReportedRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.image.service.InMemoryTempPostService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.CommunityUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityUpdateService {
    private final ValidationUtil validationUtil;
    private final CommunityRepository communityRepository;
    private final InMemoryTempPostService inMemoryTempPostService;
    private final CommunityUtil communityUtil;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final CommunityReportedRepository communityReportedRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentReportedRepository commentReportedRepository;
    private final CommentLikeRepository commentLikeRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 최종 게시글 생성 (임시 postId(음수)를 실제 postId로 전환) */
    @Transactional
    public CommunityRespDTO finalizePost(String userEmail, PostFinalizeReqDTO reqDTO) {
        User user = validateUserAndHashtag(userEmail, reqDTO.getHashtag());
        inMemoryTempPostService.validateTempId(reqDTO.getTempPostId(), user.getUserId());

        // 게시글 생성
        Community community = createCommunity(reqDTO, user);

        // 임시 postId를 실제 postId로 업데이트
        int realPostId = community.getPostId();
        imageService.updateTargetId("community", reqDTO.getTempPostId(), realPostId);
        inMemoryTempPostService.removeTempId(reqDTO.getTempPostId());

        // 최종 게시글 생성 후, 사용되지 않는 이미지 삭제
        communityUtil.cleanupUnusedImages(community);

        return new CommunityRespDTO(community, imageService);
    }

    /** 게시글 수정 */
    @Transactional
    public CommunityRespDTO updatePost(CommunityReqDTO postDTO, Integer postId, String userEmail) {
        User user = validateUserAndHashtag(userEmail, postDTO.getHashtag());
        Community post = validationUtil.validatePostExists(postId);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        // 게시글 수정
        updateCommunity(post, postDTO);

        // 게시글 수정 후, 사용되지 않는 이미지 삭제
        communityUtil.cleanupUnusedImages(post);

        return new CommunityRespDTO(post, imageService);
    }

    /** 게시글 삭제 */
    @Transactional
    public int deletePost(Integer postId, String userEmail) {
        Community post = validationUtil.validatePostExists(postId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        deleteCommunityData(postId, post);
        communityRepository.delete(post);

        return post.getCommentCount();
    }

    // 유저 및 해시태그 검증
    private User validateUserAndHashtag(String userEmail, List<String> hashtags) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateIsHashtagListValid(hashtags);
        return user;
    }

    // 게시글 생성
    private Community createCommunity(PostFinalizeReqDTO reqDTO, User user) {
        Community community = new Community();
        community.setUser(user);
        community.setTitle(reqDTO.getTitle());
        community.setContents(reqDTO.getContent());
        community.setCreatedAt(LocalDateTime.now(KST));
        community.setLikeCount(0);
        community.setCommentCount(0);
        community.setPreview(communityUtil.getPreview(reqDTO.getContent())); // 미리보기 반영
        community.setHashtags(reqDTO.getHashtag()); //해시태그 반영
        communityRepository.save(community);

        return community;
    }

    // 게시글 수정
    private void updateCommunity(Community community, CommunityReqDTO postDTO) {
        community.setTitle(postDTO.getTitle());
        community.setContents(postDTO.getContent());
        community.setPreview(communityUtil.getPreview(postDTO.getContent())); // 미리보기 반영
        community.setHashtags(postDTO.getHashtag()); // 해시태그 반영
        communityRepository.save(community);
    }

    // 게시글 삭제
    private void deleteCommunityData(Integer postId, Community post) {
        communityReportedRepository.deleteAll(communityReportedRepository.findByCommunity_PostId(postId));
        communityLikeRepository.deleteAll(communityLikeRepository.findByCommunity(post));

        List<Comment> comments = commentRepository.findByCommunity(post);
        for (Comment comment : comments) {
            commentReportedRepository.deleteAll(commentReportedRepository.findByComment_CommentId(comment.getCommentId()));
            commentLikeRepository.deleteAll(commentLikeRepository.findByComment_CommentId(comment.getCommentId()));
        }
        commentRepository.deleteAll(comments);

        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("community", postId);
        for (Image image : images) {
            imageService.deleteImage(image.getImageId());
        }
    }
}