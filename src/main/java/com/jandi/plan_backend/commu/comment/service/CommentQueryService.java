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
import com.jandi.plan_backend.util.CommentUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {
    private final ValidationUtil validationUtil;
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final CommentUtil commentUtil;

    /** 댓글 목록 조회 (배치 조회로 N+1 최적화) */
    @Transactional(readOnly = true)
    public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size, String userEmail) {
        validationUtil.validatePostExists(postId);

        User currentUser = (userEmail == null) ? null :
                userRepository.findByEmail(userEmail).orElse(null);

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        
        return PaginationService.getPagedDataBatch(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                (comments, pageable) -> {
                    // 배치 조회: User 맵, 좋아요 Set을 한 번에 조회
                    Map<Integer, User> userMap = commentUtil.getCommentUsersMap(comments);
                    Set<Integer> likedIds = commentUtil.getLikedCommentIds(comments, currentUser);
                    
                    return comments.stream()
                            .map(comment -> {
                                User author = userMap.get(comment.getUserId());
                                boolean liked = likedIds.contains(comment.getCommentId());
                                return new ParentCommentDTO(comment, author, imageService, liked);
                            })
                            .collect(Collectors.toList());
                });
    }

    /** 답글 목록 조회 (배치 조회로 N+1 최적화) */
    @Transactional(readOnly = true)
    public Page<RepliesDTO> getAllReplies(Integer commentId, int page, int size, String userEmail) {
        validationUtil.validateCommentExists(commentId);

        User currentUser = (userEmail == null) ? null :
                userRepository.findByEmail(userEmail).orElse(null);

        long totalCount = commentRepository.countByParentCommentCommentId(commentId);
        
        return PaginationService.getPagedDataBatch(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(commentId, pageable),
                (replies, pageable) -> {
                    // 배치 조회: User 맵, 좋아요 Set을 한 번에 조회
                    Map<Integer, User> userMap = commentUtil.getCommentUsersMap(replies);
                    Set<Integer> likedIds = commentUtil.getLikedCommentIds(replies, currentUser);
                    
                    return replies.stream()
                            .map(reply -> {
                                User author = userMap.get(reply.getUserId());
                                boolean liked = likedIds.contains(reply.getCommentId());
                                return new RepliesDTO(reply, author, imageService, liked);
                            })
                            .collect(Collectors.toList());
                });
    }
}
