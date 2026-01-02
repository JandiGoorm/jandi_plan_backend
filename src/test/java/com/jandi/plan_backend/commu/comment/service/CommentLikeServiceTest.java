package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.entity.CommentLike;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.fixture.CommentFixture;
import com.jandi.plan_backend.fixture.CommunityFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentLikeService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 댓글 좋아요, 좋아요 취소
 * 테스트 기법: 동등분할 (유효/무효 입력), 경계값 분석
 */
@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentLikeService commentLikeService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private Community community;
    private Comment comment;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        community = CommunityFixture.createCommunity(otherUser);
        comment = CommentFixture.createComment(otherUser, community); // 타인의 댓글
    }

    // ==================== 댓글 좋아요 테스트 ====================

    @Nested
    @DisplayName("댓글 좋아요")
    class LikeCommentTest {

        @Test
        @DisplayName("[성공] 타인의 댓글에 좋아요")
        void likeComment_ToOthersComment_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(comment);
            when(commentLikeRepository.findByCommentAndUser(comment, normalUser)).thenReturn(Optional.empty());
            when(commentLikeRepository.save(any(CommentLike.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            commentLikeService.likeComment(userEmail, commentId);

            // then
            verify(commentLikeRepository).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("[실패] 본인의 댓글에 좋아요 시 예외 발생")
        void likeComment_ToOwnComment_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;
            Comment myComment = CommentFixture.createComment(normalUser, community); // 본인의 댓글

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(myComment);

            // when & then
            assertThatThrownBy(() -> commentLikeService.likeComment(userEmail, commentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("본인의 댓글에 좋아요할 수 없습니다");

            verify(commentLikeRepository, never()).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("[실패] 이미 좋아요한 댓글에 중복 좋아요 시 예외 발생")
        void likeComment_AlreadyLiked_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(comment);
            when(commentLikeRepository.findByCommentAndUser(comment, normalUser)).thenReturn(Optional.of(new CommentLike()));

            // when & then
            assertThatThrownBy(() -> commentLikeService.likeComment(userEmail, commentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 좋아요한 댓글입니다");

            verify(commentLikeRepository, never()).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 좋아요 시 예외 발생")
        void likeComment_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> commentLikeService.likeComment(userEmail, commentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");

            verify(commentLikeRepository, never()).save(any(CommentLike.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 댓글에 좋아요 시 예외 발생")
        void likeComment_ToNonExistentComment_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentCommentId = 9999;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(nonExistentCommentId))
                    .thenThrow(new BadRequestExceptionMessage("해당 댓글을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> commentLikeService.likeComment(userEmail, nonExistentCommentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }
    }

    // ==================== 댓글 좋아요 취소 테스트 ====================

    @Nested
    @DisplayName("댓글 좋아요 취소")
    class DeleteLikeCommentTest {

        @Test
        @DisplayName("[성공] 좋아요한 댓글 좋아요 취소")
        void deleteLikeComment_WithExistingLike_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;
            CommentLike existingLike = new CommentLike();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(comment);
            when(commentLikeRepository.findByCommentAndUser(comment, normalUser))
                    .thenReturn(Optional.of(existingLike));

            // when
            commentLikeService.deleteLikeComment(userEmail, commentId);

            // then
            verify(commentLikeRepository).delete(existingLike);
        }

        @Test
        @DisplayName("[실패] 좋아요하지 않은 댓글 좋아요 취소 시 예외 발생")
        void deleteLikeComment_WithoutExistingLike_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(comment);
            when(commentLikeRepository.findByCommentAndUser(comment, normalUser))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentLikeService.deleteLikeComment(userEmail, commentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("좋아요한 적 없는 댓글입니다");

            verify(commentLikeRepository, never()).delete(any(CommentLike.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 좋아요 취소 시 예외 발생")
        void deleteLikeComment_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> commentLikeService.deleteLikeComment(userEmail, commentId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");
        }
    }
}
