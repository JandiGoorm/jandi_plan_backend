package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.entity.CommunityLike;
import com.jandi.plan_backend.commu.entity.CommunityReported;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityLikeRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.commu.repository.CommunityReportedRepository;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PostService {

    private final CommunityRepository communityRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final CommunityReportedRepository communityReportedRepository;
    private final CommunityLikeRepository communityLikeRepository;

    // ↓↓↓ 추가: 임시 postId(음수) 관리를 위한 서비스
    private final InMemoryTempPostService inMemoryTempPostService;

    // 생성자 주입
    public PostService(
            CommunityRepository communityRepository,
            ValidationUtil validationUtil,
            ImageService imageService,
            CommentRepository commentRepository,
            ImageRepository imageRepository,
            CommunityReportedRepository communityReportedRepository,
            CommunityLikeRepository communityLikeRepository,
            InMemoryTempPostService inMemoryTempPostService  // ← 추가
    ) {
        this.communityRepository = communityRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.commentRepository = commentRepository;
        this.imageRepository = imageRepository;
        this.communityReportedRepository = communityReportedRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.inMemoryTempPostService = inMemoryTempPostService; // ← 필드 초기화
    }

    /** 특정 게시글 조회 */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId) {
        //게시글의 존재 여부 검증
        Community community = validationUtil.validatePostExists(postId);

        //게시글 조회수 카운팅
        community.setViewCount(community.getViewCount() + 1);
        communityRepository.save(community);

        //게시글 반환
        Optional<Community> post = communityRepository.findByPostId(postId);
        return post.map(p -> new CommunityItemDTO(p, imageService)); // imageService 포함
    }

    /** 게시글 목록 전체 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();

        // postId 내림차순 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "postId");

        return PaginationService.getPagedData(page, size, totalCount,
                (pageable) -> communityRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)),
                community -> new CommunityListDTO(community, imageService));
    }

    /** 게시글 작성 */
    public CommunityRespDTO writePost(CommunityReqDTO postDTO, String userEmail) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 게시글 생성
        Community community = new Community();
        community.setUser(user);
        community.setTitle(postDTO.getTitle());
        community.setContents(postDTO.getContent());
        community.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        community.setLikeCount(0);
        community.setCommentCount(0);

        // DB 저장 및 반환
        communityRepository.save(community);
        return new CommunityRespDTO(community, imageService);
    }

    /**
     * 최종 게시글 생성 (임시 postId(음수) → 실제 postId)
     */
    public CommunityRespDTO finalizePost(String userEmail, PostFinalizeReqDTO reqDTO) {
        // 1) 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 2) 임시 postId 검증 (InMemoryTempPostService)
        inMemoryTempPostService.validateTempId(reqDTO.getTempPostId(), user.getUserId());

        // 3) 실제 게시글 생성
        Community community = new Community();
        community.setUser(user);
        community.setTitle(reqDTO.getTitle());
        community.setContents(reqDTO.getContent());
        community.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        community.setLikeCount(0);
        community.setCommentCount(0);
        communityRepository.save(community);

        int realPostId = community.getPostId();

        // 4) 이미지 targetId 업데이트 (임시 → 실제)
        // (예) imageService.updateTargetId("community", -1234567, realPostId);
        imageService.updateTargetId("community", reqDTO.getTempPostId(), realPostId);

        // 5) 임시 postId 제거
        inMemoryTempPostService.removeTempId(reqDTO.getTempPostId());

        // 6) 반환
        return new CommunityRespDTO(community, imageService);
    }

    /** 게시물 수정 */
    public CommunityRespDTO updatePost(CommunityReqDTO postDTO, Integer postId, String userEmail) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        validationUtil.validateUserIsAuthorOfPost(user, post);

        // 게시글 수정
        post.setTitle(postDTO.getTitle());
        post.setContents(postDTO.getContent());

        // DB 저장 및 반환
        communityRepository.save(post);
        return new CommunityRespDTO(post, imageService);
    }

    /** 게시물 삭제 */
    public int deletePost(Integer postId, String userEmail) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        if(user.getUserId()!=1) validationUtil.validateUserIsAuthorOfPost(user, post);

        //하위 댓글 모두 삭제
        List<Comment> comments = commentRepository.findByCommunity(post);
        int commentsCount = comments.size();
        commentRepository.deleteAll(comments);

        //연결된 이미지 모두 삭제
        imageRepository.findByTargetTypeAndTargetId("community", postId)
                .ifPresent(image -> imageService.deleteImage(image.getImageId()));

        // 게시물 삭제 및 삭제된 댓글 수 반환
        communityRepository.delete(post);
        return commentsCount;
    }

    /** 게시물 좋아요 */
    public void likePost(String userEmail, Integer postId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        // 좋아요 검증
        if(user.getUserId().equals(post.getUser().getUserId())){ // 셀프 좋아요 방지
            throw new BadRequestExceptionMessage("본인의 게시글에 좋아요할 수 없습니다.");
        }
        if(communityLikeRepository.findByCommunityAndUser(post, user).isPresent()){ // 중복 좋아요 방지
            throw new BadRequestExceptionMessage("이미 좋아요한 게시물입니다.");
        }

        // community_like에 반영
        CommunityLike communityLike = new CommunityLike();
        communityLike.setCommunity(post);
        communityLike.setUser(user);
        communityLike.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        communityLikeRepository.save(communityLike);

        // 게시물 좋아요 수 증가
        post.setLikeCount(post.getLikeCount() + 1);
        communityRepository.save(post);
    }

    /** 게시물 좋아요 취소 */
    public void deleteLikePost(String userEmail, Integer postId) {
        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        //좋아요 검증
        Optional<CommunityLike> communityLike = communityLikeRepository.findByCommunityAndUser(post, user);
        if(communityLike.isEmpty()){ // 좋아요되지 않은 게시물일 때
            throw new BadRequestExceptionMessage("좋아요한 적 없는 게시물입니다.");
        }
        communityLikeRepository.delete(communityLike.get());
        post.setLikeCount(post.getLikeCount() - 1);
        communityRepository.save(post);
    }

    /** 게시물 신고 */
    public PostReportRespDTO reportPost(String userEmail, Integer postId, ReportReqDTO reportDTO) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);

        // 유저 검증
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        // 중복 신고 방지
        if(communityReportedRepository.findByUser_userIdAndCommunity_postId(user.getUserId(), postId).isPresent()){
            throw new BadRequestExceptionMessage("이미 신고한 게시글입니다.");
        }

        // 게시물 신고 생성
        CommunityReported communityReported = new CommunityReported();
        communityReported.setUser(user);
        communityReported.setCommunity(post);
        communityReported.setContents(reportDTO.getContents());
        communityReported.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        communityReportedRepository.save(communityReported);

        return new PostReportRespDTO(communityReported);
    }

}
