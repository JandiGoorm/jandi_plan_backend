package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.CommentReportedListDTO;
import com.jandi.plan_backend.commu.dto.CommunityReportedListDTO;
import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.repository.CommunityReportedRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManageCommunityService {
    private final ValidationUtil validationUtil;
    private final CommunityReportedRepository communityReportedRepository;
    private final CommentReportedRepository commentReportedRepository;
    private final PostService postService;
    private final CommentService commentService;

    public ManageCommunityService(
            ValidationUtil validationUtil,
            CommunityReportedRepository communityReportedRepository,
            CommentReportedRepository commentReportedRepository, PostService postService, CommentService commentService) {
        this.validationUtil = validationUtil;
        this.communityReportedRepository = communityReportedRepository;
        this.commentReportedRepository = commentReportedRepository;
        this.postService = postService;
        this.commentService = commentService;
    }

    public Page<CommunityReportedListDTO> getReportedPosts(String userEmail, int page, int size) {
        //유저 검증
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        long totalCount = communityReportedRepository.count(); // 전체 신고된 게시글 수
        return PaginationService.getPagedData(page, size, totalCount,
                communityReportedRepository::findReportedCommunitiesWithCount,  // 데이터 조회
                ReportedObj -> {
                    Community community = (Community) ReportedObj[0];  // 신고된 게시글
                    Integer reportCount = ((Number) ReportedObj[1]).intValue();  // 신고 횟수
                    return new CommunityReportedListDTO(community, reportCount);
                }
        );
    }

    // 신고 댓글 조회
    public Page<CommentReportedListDTO> getReportedComments(String userEmail, int page, int size) {
        //유저 검증
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        long totalCount = commentReportedRepository.count(); // 전체 신고된 게시글 수
        return PaginationService.getPagedData(page, size, totalCount,
                commentReportedRepository::findReportedCommentsWithCount,  // 데이터 조회
                ReportedObj -> {
                    Comment comment = (Comment) ReportedObj[0];  // 신고된 댓글
                    Integer reportCount = ((Number) ReportedObj[1]).intValue();  // 신고 횟수
                    return new CommentReportedListDTO(comment, reportCount);
                }
        );
    }

    // 게시글 강제 삭제
    public void deletePosts(String userEmail, Integer postId) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        Community post = validationUtil.validatePostExists(postId);
        postService.deletePost(postId, post.getUser().getEmail());
    }

    // 댓글 강제 삭제
    public void deleteComments(String userEmail, Integer commentId) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        Comment comment = validationUtil.validateCommentExists(commentId);
        User user = validationUtil.validateUserExists(comment.getUserId());
        commentService.deleteComments(commentId, user.getEmail());
    }
}
