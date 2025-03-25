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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
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

    public CommunityUpdateService(
            ValidationUtil validationUtil,
            CommunityRepository communityRepository,
            InMemoryTempPostService inMemoryTempPostService,
            CommunityUtil communityUtil, ImageService imageService,
            ImageRepository imageRepository,
            CommunityReportedRepository communityReportedRepository,
            CommunityLikeRepository communityLikeRepository,
            CommentRepository commentRepository,
            CommentReportedRepository commentReportedRepository,
            CommentLikeRepository commentLikeRepository
    ) {
        this.validationUtil = validationUtil;
        this.communityRepository = communityRepository;
        this.inMemoryTempPostService = inMemoryTempPostService;
        this.communityUtil = communityUtil;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.communityReportedRepository = communityReportedRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.commentRepository = commentRepository;
        this.commentReportedRepository = commentReportedRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    /** 최종 게시글 생성 (임시 postId(음수)를 실제 postId로 전환) */
    public CommunityRespDTO finalizePost(String userEmail, PostFinalizeReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateIsHashtagListValid(reqDTO.getHashtag());
        inMemoryTempPostService.validateTempId(reqDTO.getTempPostId(), user.getUserId());

        Community community = new Community();
        community.setUser(user);
        community.setTitle(reqDTO.getTitle());
        community.setContents(reqDTO.getContent());
        community.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        community.setLikeCount(0);
        community.setCommentCount(0);
        community.setPreview(communityUtil.getPreview(reqDTO.getContent())); // 미리보기 반영
        community.setHashtags(reqDTO.getHashtag()); //해시태그 반영
        communityRepository.save(community);

        int realPostId = community.getPostId();

        // 임시 postId를 실제 postId로 업데이트
        imageService.updateTargetId("community", reqDTO.getTempPostId(), realPostId);
        inMemoryTempPostService.removeTempId(reqDTO.getTempPostId());

        // 최종 게시글 생성 후, 사용되지 않는 이미지 삭제
        communityUtil.cleanupUnusedImages(community);

        return new CommunityRespDTO(community, imageService);
    }

    /** 게시글 수정 */
    public CommunityRespDTO updatePost(CommunityReqDTO postDTO, Integer postId, String userEmail) {
        Community post = validationUtil.validatePostExists(postId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfPost(user, post);
        validationUtil.validateIsHashtagListValid(postDTO.getHashtag());


        post.setTitle(postDTO.getTitle());
        post.setContents(postDTO.getContent());
        post.setPreview(communityUtil.getPreview(postDTO.getContent())); // 미리보기 반영
        post.setHashtags(postDTO.getHashtag()); // 해시태그 반영
        communityRepository.save(post);

        // 게시글 수정 후, 사용되지 않는 이미지 삭제
        communityUtil.cleanupUnusedImages(post);

        return new CommunityRespDTO(post, imageService);
    }

    /** 게시글 삭제 */
    public int deletePost(Integer postId, String userEmail) {
        Community post = validationUtil.validatePostExists(postId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        // 게시글의 신고 및 좋아요 정보 삭제
        communityReportedRepository.deleteAll(communityReportedRepository.findByCommunity_PostId(postId));
        communityLikeRepository.deleteAll(communityLikeRepository.findByCommunity(post));

        // 게시글에 달린 모든 댓글 및 댓글의 신고/좋아요 정보 삭제
        List<Comment> comments = commentRepository.findByCommunity(post);
        int commentsCount = comments.size();
        for (Comment comment : comments) {
            commentReportedRepository.deleteAll(commentReportedRepository.findByComment_CommentId(comment.getCommentId()));
            commentLikeRepository.deleteAll(commentLikeRepository.findByComment_CommentId(comment.getCommentId()));
        }
        commentRepository.deleteAll(comments);

        // 게시글과 연결된 모든 이미지 삭제
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("community", postId);
        for (Image image : images) {
            imageService.deleteImage(image.getImageId());
        }

        communityRepository.delete(post);
        return commentsCount;
    }
}
