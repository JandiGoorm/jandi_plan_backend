package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.dto.CommentReqDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentRespDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentUpdateService {
    private final ValidationUtil validationUtil;
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReportedRepository commentReportedRepository;

    /** 댓글 작성 */
    @Transactional
    public CommentRespDTO writeComment(CommentReqDTO commentDTO, Integer postId, String userEmail) {
        User user = validateUser(userEmail);
        Community post = validationUtil.validatePostExists(postId);

        // 댓글 추가
        Comment newComment = saveCommentData(user, null, post, commentDTO.getContents());

        // 댓글 수 증가 처리
        commentRepository.increaseCommentCount(post.getPostId(), 1);

        return new CommentRespDTO(newComment, imageService);
    }

    /** 답글 작성 */
    @Transactional
    public CommentRespDTO writeReplies(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        User user = validateUser(userEmail);

        Comment parentComment = validationUtil.validateCommentExists(commentId);
        Community post = parentComment.getCommunity();

        // 답글 추가
        Comment newComment = saveCommentData(user, parentComment, post, commentDTO.getContents());

        // 댓글 수, 답글 수 증가 처리
        commentRepository.increaseCommentCount(post.getPostId(), 1);
        commentRepository.increaseRepliesCount(commentId);

        return new CommentRespDTO(newComment, imageService);
    }

    /** 댓글 수정 */
    @Transactional
    public CommentRespDTO updateComment(CommentReqDTO commentDTO, Integer commentId, String userEmail) {
        User user = validateUser(userEmail);
        Comment comment = validateUsersComment(user, commentId);

        // 댓글 수정
        updateCommentData(comment, commentDTO.getContents());
        return new CommentRespDTO(comment, imageService);
    }

    /** 댓글 및 답글 삭제 */
    @Transactional
    public int deleteComments(Integer commentId, String userEmail) {
        User user = validationUtil.validateUserExists(userEmail);
        Comment comment = validateUsersComment(user, commentId);

        // getParentComment()가 null이면 댓글로 추가, null이 아니면 답글로 추가
        return (comment.getParentComment() == null) ?
                deleteCommentData(comment) : deleteReplyData(comment);
    }

    // 유저의 존재 여부와 작성 가능 여부 한번에 검증
    private User validateUser(String userEmail){
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        return user;
    }

    // 댓글의 존재 여부와 본인 댓글인지를 한번에 검증
    private Comment validateUsersComment(User user, Integer commentId){
        Comment comment = validationUtil.validateCommentExists(commentId);
        validationUtil.validateUserIsAuthorOfComment(user, comment);
        return comment;
    }

    // 댓글 저장 메서드 (댓글 또는 답글)
    private Comment saveCommentData(User user, Comment parentComment, Community post, String content) {
        Comment comment = new Comment();
        comment.setCommunity(post);
        comment.setParentComment(parentComment);
        comment.setUserId(user.getUserId());
        comment.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        comment.setContents(content);
        comment.setLikeCount(0);
        comment.setRepliesCount(0);
        commentRepository.save(comment);
        return comment;
    }

    // 댓글 수정 메서드
    private void updateCommentData (Comment comment, String contents){
        comment.setContents(contents);
        commentRepository.save(comment);
    }

    // 댓글 삭제 메서드
    private Integer deleteCommentData(Comment comment) {
        Integer commentId = comment.getCommentId();

        // 본인 하위의 답글 삭제
        List<Comment> replies = commentRepository.findByParentCommentCommentId(commentId);
        int repliesCount = replies.size();
        for(Comment reply : replies) {
            deleteCommentAssociations(reply);
        }
        commentRepository.deleteAll(replies);

        // 댓글 수 감소 처리
        Integer postId = comment.getCommunity().getPostId();
        commentRepository.decreaseCommentCount(postId, 1 + repliesCount);

        // 자신 삭제
        deleteCommentAssociations(comment);
        commentRepository.delete(comment);
        return repliesCount;
    }

    // 답글 삭제 메서드
    private Integer deleteReplyData(Comment comment){
        // 댓글 수 감소 처리
        Integer postId = comment.getCommunity().getPostId();
        commentRepository.decreaseCommentCount(postId, 1);

        // 답글 수 감소 처리
        Integer parentCommentId = comment.getParentComment().getCommentId();
        commentRepository.decreaseRepliesCount(parentCommentId);

        // 자신 삭제
        deleteCommentAssociations(comment);
        commentRepository.delete(comment);
        return 0;
    }

    // 댓글의 좋아요 및 신고 정보 삭제
    private void deleteCommentAssociations(Comment comment) {
        commentLikeRepository.deleteAll(commentLikeRepository.findByComment(comment));
        commentReportedRepository.deleteAll(commentReportedRepository.findByComment(comment));
    }
}
