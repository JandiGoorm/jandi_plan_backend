package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.entity.CommunityLike;
import com.jandi.plan_backend.commu.entity.Reported;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityLikeRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.commu.repository.ReportedRepository;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.InMemoryTempPostService;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PostService {

    private final CommunityRepository communityRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final ReportedRepository reportedRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final InMemoryTempPostService inMemoryTempPostService;

    public PostService(
            CommunityRepository communityRepository,
            ValidationUtil validationUtil,
            ImageService imageService,
            CommentRepository commentRepository,
            ImageRepository imageRepository,
            ReportedRepository reportedRepository,
            CommunityLikeRepository communityLikeRepository,
            InMemoryTempPostService inMemoryTempPostService
    ) {
        this.communityRepository = communityRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.commentRepository = commentRepository;
        this.imageRepository = imageRepository;
        this.reportedRepository = reportedRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.inMemoryTempPostService = inMemoryTempPostService;
    }

    /** 특정 게시글 조회 */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId) {
        Community community = validationUtil.validatePostExists(postId);
        community.setViewCount(community.getViewCount() + 1);
        communityRepository.save(community);
        Optional<Community> post = communityRepository.findByPostId(postId);
        return post.map(p -> new CommunityItemDTO(p, imageService));
    }

    /** 게시글 목록 전체 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();
        Sort sort = Sort.by(Sort.Direction.DESC, "postId");
        return PaginationService.getPagedData(page, size, totalCount,
                (pageable) -> communityRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)),
                community -> new CommunityListDTO(community, imageService));
    }

    /**
     * 최종 게시글 생성 (임시 postId(음수)를 실제 postId로 전환)
     */
    public CommunityRespDTO finalizePost(String userEmail, PostFinalizeReqDTO reqDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        inMemoryTempPostService.validateTempId(reqDTO.getTempPostId(), user.getUserId());

        Community community = new Community();
        community.setUser(user);
        community.setTitle(reqDTO.getTitle());
        community.setContents(reqDTO.getContent());
        community.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        community.setLikeCount(0);
        community.setCommentCount(0);
        communityRepository.save(community);

        int realPostId = community.getPostId();

        // 임시 postId를 실제 postId로 업데이트
        imageService.updateTargetId("community", reqDTO.getTempPostId(), realPostId);
        inMemoryTempPostService.removeTempId(reqDTO.getTempPostId());

        // 최종 게시글 생성 후, 사용되지 않는 이미지 삭제
        cleanupUnusedImages(community);

        return new CommunityRespDTO(community, imageService);
    }

    /** 게시글 수정 */
    public CommunityRespDTO updatePost(CommunityReqDTO postDTO, Integer postId, String userEmail) {
        Community post = validationUtil.validatePostExists(postId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        post.setTitle(postDTO.getTitle());
        post.setContents(postDTO.getContent());
        communityRepository.save(post);

        // 게시글 수정 후, 사용되지 않는 이미지 삭제
        cleanupUnusedImages(post);

        return new CommunityRespDTO(post, imageService);
    }

    /** 게시글 삭제 */
    public int deletePost(Integer postId, String userEmail) {
        Community post = validationUtil.validatePostExists(postId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        if (user.getUserId() != 1) {
            validationUtil.validateUserIsAuthorOfPost(user, post);
        }
        List<Comment> comments = commentRepository.findByCommunity(post);
        int commentsCount = comments.size();
        commentRepository.deleteAll(comments);

        // 게시글과 연결된 모든 이미지 삭제
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("community", postId);
        for (Image image : images) {
            imageService.deleteImage(image.getImageId());
        }

        communityRepository.delete(post);
        return commentsCount;
    }

    /** 게시물 좋아요 */
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

        post.setLikeCount(post.getLikeCount() + 1);
        communityRepository.save(post);
    }

    /** 게시물 좋아요 취소 */
    public void deleteLikePost(String userEmail, Integer postId) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Community post = validationUtil.validatePostExists(postId);

        Optional<CommunityLike> communityLike = communityLikeRepository.findByCommunityAndUser(post, user);
        if(communityLike.isEmpty()){
            throw new BadRequestExceptionMessage("좋아요한 적 없는 게시물입니다.");
        }
        communityLikeRepository.delete(communityLike.get());
        post.setLikeCount(post.getLikeCount() - 1);
        communityRepository.save(post);
    }

    /** 게시물 신고 */
    public ReportRespDTO reportPost(String userEmail, Integer postId, ReportReqDTO reportDTO) {
        Community post = validationUtil.validatePostExists(postId);
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        if(reportedRepository.findByUser_userIdAndCommunity_postId(user.getUserId(), postId).isPresent()){
            throw new BadRequestExceptionMessage("이미 신고한 게시글입니다.");
        }

        Reported reported = new Reported();
        reported.setUser(user);
        reported.setCommunity(post);
        reported.setContents(reportDTO.getContents());
        reported.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        reportedRepository.save(reported);

        return new ReportRespDTO(reported);
    }

    /**
     * 게시글 내용에서 실제 사용 중인 이미지 파일명을 추출합니다.
     * 예: "https://storage.googleapis.com/plan-storage/encodedFileName.jpg"에서 "encodedFileName.jpg" 추출
     */
    private Set<String> extractImageFileNamesFromContent(String content) {
        Set<String> fileNames = new HashSet<>();
        Pattern pattern = Pattern.compile("https://storage.googleapis.com/plan-storage/([^\"\\s]+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            fileNames.add(matcher.group(1));
        }
        return fileNames;
    }

    /**
     * 게시글과 연결된 이미지 중, 게시글 내용에 포함되지 않은 이미지를 삭제합니다.
     */
    private void cleanupUnusedImages(Community post) {
        Set<String> usedFileNames = extractImageFileNamesFromContent(post.getContents());
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("community", post.getPostId());
        for (Image image : images) {
            if (!usedFileNames.contains(image.getImageUrl())) {
                imageService.deleteImage(image.getImageId());
            }
        }
    }

}
