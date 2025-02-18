package com.jandi.plan_backend.util;

import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** 검증 검사 Util*/
@Component
public class ValidationUtil {
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;

    public ValidationUtil(UserRepository userRepository, CommunityRepository communityRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
    }

    /** userRepository */
    // 사용자의 존재 여부 검증
    public User validateUserExists(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 사용자입니다."));
    }

    // 사용자 활동 제한 여부 검증
    public void validateUserRestricted(User user) {
        if (user.getReported()) {
            throw new BadRequestExceptionMessage("비정상적인 활동이 반복되어 게시글 작성이 제한되었습니다.");
        }
    }

    // 유저가 관리자인지 검증
    public void validateUserIsAdmin(User user) {
        if(user.getUserId() != 1)
            throw new BadRequestExceptionMessage("공지사항을 작성할 권한이 없습니다");
    }

    /** communityRepository 관련 검증 */
    // 게시글의 존재 여부 검증
    public Community validatePostExists(Integer postId) {
        return communityRepository.findByPostId(postId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 게시글입니다."));
    }

    // 게시물의 작성자인지 검증
    public void validateUserIsAuthorOfPost(User user, Community post) {
        if(!Objects.equals(user.getUserId(), post.getUser().getUserId()))
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
    }

    /** commentRepository 관련 검증 */
    // 댓글의 존재 여부 검증
    public Comments validateCommentExists(Integer commentId) {
        return (Comments) commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 댓글입니다."));
    }

    // 댓글의 작성자인지 검증
    public void validateUserIsAuthorOfComment(User user, Comments comment) {
        if(!Objects.equals(user.getUserId(), comment.getUserId()))
            throw new BadRequestExceptionMessage("작성자 본인만 수정할 수 있습니다.");
    }
}
