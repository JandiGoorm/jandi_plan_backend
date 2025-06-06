package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.comment.dto.CommentReportedListDTO;
import com.jandi.plan_backend.commu.comment.service.CommentUpdateService;
import com.jandi.plan_backend.commu.community.dto.CommunityReportedListDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityReportedRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ManageCommunityService {
    private final ValidationUtil validationUtil;
    private final CommunityReportedRepository communityReportedRepository;
    private final CommentReportedRepository commentReportedRepository;
    private final CommentUpdateService commentUpdateService;
    private final CommunityUpdateService communityUpdateService;

    public ManageCommunityService(
            ValidationUtil validationUtil,
            CommunityReportedRepository communityReportedRepository,
            CommentReportedRepository commentReportedRepository, CommentUpdateService commentUpdateService, CommunityUpdateService communityUpdateService) {
        this.validationUtil = validationUtil;
        this.communityReportedRepository = communityReportedRepository;
        this.commentReportedRepository = commentReportedRepository;
        this.commentUpdateService = commentUpdateService;
        this.communityUpdateService = communityUpdateService;
    }

    public Page<CommunityReportedListDTO> getReportedPosts(String userEmail, int page, int size) {
        //유저 검증
        User admin = validationUtil.validateUserExists(userEmail);

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
    public void deletePosts(Integer postId) {
        Community post = validationUtil.validatePostExists(postId);
        communityUpdateService.deletePost(postId, post.getUser().getEmail());
    }

    // 댓글 강제 삭제
    public void deleteComments(Integer commentId) {
        Comment comment = validationUtil.validateCommentExists(commentId);
        User user = validationUtil.validateUserExists(comment.getUserId());
        commentUpdateService.deleteComments(commentId, user.getEmail());
    }
}
