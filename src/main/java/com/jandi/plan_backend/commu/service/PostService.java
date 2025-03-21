package com.jandi.plan_backend.commu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.entity.CommunityLike;
import com.jandi.plan_backend.commu.entity.CommunityReported;
import com.jandi.plan_backend.commu.repository.*;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.image.service.InMemoryTempPostService;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final CommunityReportedRepository communityReportedRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final InMemoryTempPostService inMemoryTempPostService;
    private final UserRepository userRepository;
    private final CommentReportedRepository commentReportedRepository;
    private final CommentLikeRepository commentLikeRepository;
    String prefix = "https://storage.googleapis.com/plan-storage/";
    int maxPreviewLength = 200;

    // 생성자 주입
    public PostService(
            CommunityRepository communityRepository,
            ValidationUtil validationUtil,
            ImageService imageService,
            CommentRepository commentRepository,
            ImageRepository imageRepository,
            CommunityReportedRepository communityReportedRepository,
            CommunityLikeRepository communityLikeRepository,
            InMemoryTempPostService inMemoryTempPostService,
            UserRepository userRepository,
            CommentReportedRepository commentReportedRepository, CommentLikeRepository commentLikeRepository) {
        this.communityRepository = communityRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.commentRepository = commentRepository;
        this.imageRepository = imageRepository;
        this.communityReportedRepository = communityReportedRepository;
        this.communityLikeRepository = communityLikeRepository;
        this.inMemoryTempPostService = inMemoryTempPostService;
        this.userRepository = userRepository;
        this.commentReportedRepository = commentReportedRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    /** 특정 게시글 조회 */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId, String userEmail) {
        //게시글의 존재 여부 검증
        Community community = validationUtil.validatePostExists(postId);
        community.setViewCount(community.getViewCount() + 1);
        communityRepository.save(community);

        //게시글 좋아요 여부
        User user = userRepository.findByEmail(userEmail).orElse(null);
        boolean isLike = user != null && communityLikeRepository.findByCommunityAndUser(community, user).isPresent();

        //게시글 반환
        Optional<Community> post = communityRepository.findByPostId(postId);
        return post.map(p -> new CommunityItemDTO(p, imageService, isLike)); // imageService 포함
    }

    /** 게시글 목록 전체 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();
        Sort sort = Sort.by(Sort.Direction.DESC, "postId");
        return PaginationService.getPagedData(page, size, totalCount,
                (pageable) -> communityRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)),
                community -> {
                    // 썸네일 찾기
                    List<Image> thumbnails = imageRepository.findAllByTargetTypeAndTargetId("community", community.getPostId());
                    String thumbnail = (thumbnails.isEmpty()) ? "" : prefix + thumbnails.get(0).getImageUrl();

                    return new CommunityListDTO(community, imageService, thumbnail);
                });
    }

    /**
     * 최종 게시글 생성 (임시 postId(음수)를 실제 postId로 전환)
     */
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
        community.setPreview(getPreview(reqDTO.getContent())); // 미리보기 반영
        community.setHashtags(reqDTO.getHashtag()); //해시태그 반영
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
        validationUtil.validateIsHashtagListValid(postDTO.getHashtag());


        post.setTitle(postDTO.getTitle());
        post.setContents(postDTO.getContent());
        post.setPreview(getPreview(postDTO.getContent())); // 미리보기 반영
        post.setHashtags(postDTO.getHashtag()); // 해시태그 반영
        communityRepository.save(post);

        // 게시글 수정 후, 사용되지 않는 이미지 삭제
        cleanupUnusedImages(post);

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
    public PostReportRespDTO reportPost(String userEmail, Integer postId, ReportReqDTO reportDTO) {
        //게시글 검증
        Community post = validationUtil.validatePostExists(postId);
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

    //DEV용: 기존 게시글도 미리보기 내용을 추가하기 위해 작성한 메서드
    public void patchPreview(){
        communityRepository.findAll().forEach(community -> {
            log.info("[현재 게시글] postId: {}", community.getPostId());
            community.setPreview(getPreview(community.getContents()));
            communityRepository.save(community);
        });
    }

    /**
     * 엔터는 ""로 치환 후, 최대 200자 길이의 미리보기 내용을 추출합니다.
     */
    private String getPreview (String contents) {
        StringBuilder preview = new StringBuilder();
        try {
            // contents를 ObjectMapper로 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(contents);

            // contents 의 구조: {"ops":[{"insert":"..."},{"attributes":{"code-block":"plain"},..}
            // 여기서 실제 추출해야 할 부분은 insert의 값이므로, for문을 돌면서 글자수가 채워질 때까지 insert 안의 내용을 순차적으로 더해갑니다
            for(JsonNode opsNode : node.get("ops")) {
                // 남은 preview의 길이 계산해서 공간 없다면
                int leftLength = maxPreviewLength - preview.length();
                if(leftLength < 1) break;

                JsonNode insertNode = opsNode.get("insert");
                if(insertNode != null) {
                    //원문에서 \n은 ""으로 치환
                    String insertNodeText = insertNode.asText().replace("\n", "");

                    log.info("선택된 insertNodeText: {}", insertNodeText);
                    preview.append((insertNodeText.length() > leftLength) ? //넘치면 잘라내기
                            insertNodeText.substring(0, leftLength) : insertNodeText);
                }
            }
            log.info("최종 preview: {}", preview);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return preview.toString();
    }

    public Page<CommunityListDTO> search(String category, String keyword, int page, int size) {
        //category 예외 처리
        if(category == null || category.isEmpty()) {
            throw new BadRequestExceptionMessage("카테고리를 입력하세요");
        }

        //keyword 예외 처리
        if(keyword == null || keyword.isEmpty()){
            throw new BadRequestExceptionMessage("검색어를 입력하세요.");
        }else if(keyword.trim().length() < 2){
            throw new BadRequestExceptionMessage("검색어는 2글자 이상이어야 합니다");
        }

        // 검색
        List<Community> searchList = switch (category) {
            case "TITLE" -> // 제목 검색
                communityRepository.searchAllByTitleContaining(keyword);
            case "CONTENT" -> // 내용 검색
                communityRepository.searchAllByContentsContaining(keyword);
            case "BOTH" -> // 제목 + 내용 검색
                communityRepository.searchByTitleAndContents("\"" + keyword + "\""); //공백 포함하여 계산되도록 따옴표로 래핑
            case "HASHTAG" -> { // 해시태그 검색
                validationUtil.validateIsHashTagValid(keyword); //키워드가 해시태그대로 들어왔는지 검증
                yield communityRepository.searchByHashTag("\"" + keyword + "\"");
            }
            default ->
                throw new IllegalStateException("카테고리 지정이 잘못되었습니다: " + category);
        };
        searchList.sort(Comparator.comparing(Community::getPostId).reversed()); // postId 내림차순으로 정렬
        long totalCount = searchList.size();

        return PaginationService.getPagedData(
                page, size, totalCount,
                (pageable) -> {
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), searchList.size());
                    List<Community> pagedList = searchList.subList(start, end);
                    return new PageImpl<>(pagedList, pageable, totalCount);
                },
                community -> {
                    // 썸네일 찾기
                    List<Image> thumbnails = imageRepository.findAllByTargetTypeAndTargetId("community", community.getPostId());
                    String thumbnail = (thumbnails.isEmpty()) ? "" : prefix + thumbnails.get(0).getImageUrl();

                    return new CommunityListDTO(community, imageService, thumbnail);
                }
        );
    }
}
