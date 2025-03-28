package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.dto.CommentReportRespDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentReqDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentRespDTO;
import com.jandi.plan_backend.commu.comment.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.community.dto.*;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.entity.CommentLike;
import com.jandi.plan_backend.commu.comment.entity.CommentReported;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {
    private final ValidationUtil validationUtil;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ImageService imageService;
    private final UserRepository userRepository; // 최신 사용자 정보를 조회하기 위함

    /** 댓글 목록 조회 */
    public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size, String userEmail) {
        validationUtil.validatePostExists(postId); // 게시글 존재 검증

        // 현재 요청자의 최신 정보를 조회 (로그인이 되어 있다면)
        User currentUser = (userEmail != null)
                ? userRepository.findByEmail(userEmail).orElse(null)
                : null;

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                comment -> {
                    // 최신 사용자 정보 (댓글 작성자) 조회
                    User commentAuthor = userRepository.findById(comment.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + comment.getUserId()));
                    boolean liked = (currentUser != null &&
                            commentLikeRepository.findByCommentAndUser(comment, currentUser).isPresent());
                    return new ParentCommentDTO(comment, commentAuthor, imageService, liked);
                });
    }

    /** 답글 목록 조회 */
    public Page<RepliesDTO> getAllReplies(Integer commentId, int page, int size, String userEmail) {
        validationUtil.validateCommentExists(commentId); // 댓글 존재 검증

        User currentUser = (userEmail != null)
                ? userRepository.findByEmail(userEmail).orElse(null)
                : null;

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                reply -> {
                    User replyAuthor = userRepository.findById(reply.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + reply.getUserId()));
                    boolean liked = (currentUser != null &&
                            commentLikeRepository.findByCommentAndUser(reply, currentUser).isPresent());
                    return new RepliesDTO(reply, replyAuthor, imageService, liked);
                });
    }
}
