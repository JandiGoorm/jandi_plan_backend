package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.dto.CommentReqDTO;
import com.jandi.plan_backend.commu.comment.dto.CommentRespDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.fixture.CommentFixture;
import com.jandi.plan_backend.fixture.CommunityFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.image.service.ImageService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentUpdateService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 댓글 작성, 답글 작성, 댓글 수정, 댓글 삭제
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class CommentUpdateServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentReportedRepository commentReportedRepository;

    @InjectMocks
    private CommentUpdateService commentUpdateService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private Community community;
    private Comment parentComment;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createNormalUser();
        otherUser.setUserId(999);
        otherUser.setEmail("other@example.com");

        community = CommunityFixture.createCommunity(normalUser);
        parentComment = CommentFixture.createComment(normalUser, community);
    }

    // ==================== 댓글 작성 테스트 ====================

    @Nested
    @DisplayName("댓글 작성")
    class WriteCommentTest {

        @Test
        @DisplayName("[성공] 유효한 요청으로 댓글 작성")
        void writeComment_WithValidRequest_ShouldCreateComment() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;
            CommentReqDTO reqDTO = CommentFixture.createCommentReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                saved.setCommentId(1);
                return saved;
            });
            doNothing().when(commentRepository).increaseCommentCount(postId, 1);

            // when
            CommentRespDTO result = commentUpdateService.writeComment(reqDTO, postId, userEmail);

            // then
            assertThat(result).isNotNull();
            verify(commentRepository).save(any(Comment.class));
            verify(commentRepository).increaseCommentCount(postId, 1);
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 댓글 작성 시 예외 발생")
        void writeComment_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer postId = 1;
            CommentReqDTO reqDTO = CommentFixture.createCommentReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> commentUpdateService.writeComment(reqDTO, postId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("활동이 제한된 사용자");

            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    // ==================== 답글 작성 테스트 ====================

    @Nested
    @DisplayName("답글 작성")
    class WriteRepliesTest {

        @Test
        @DisplayName("[성공] 유효한 요청으로 답글 작성")
        void writeReplies_WithValidRequest_ShouldCreateReply() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;
            CommentReqDTO reqDTO = CommentFixture.createCommentReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(parentComment);
            when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                saved.setCommentId(100);
                return saved;
            });
            doNothing().when(commentRepository).increaseCommentCount(anyInt(), eq(1));
            doNothing().when(commentRepository).increaseRepliesCount(commentId);

            // when
            CommentRespDTO result = commentUpdateService.writeReplies(reqDTO, commentId, userEmail);

            // then
            assertThat(result).isNotNull();
            verify(commentRepository).save(any(Comment.class));
            verify(commentRepository).increaseRepliesCount(commentId);
        }
    }

    // ==================== 댓글 수정 테스트 ====================

    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTest {

        @Test
        @DisplayName("[성공] 작성자가 댓글 수정")
        void updateComment_ByAuthor_ShouldUpdateSuccessfully() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;
            CommentReqDTO reqDTO = CommentFixture.createUpdateReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(parentComment);
            doNothing().when(validationUtil).validateUserIsAuthorOfComment(normalUser, parentComment);

            // when
            CommentRespDTO result = commentUpdateService.updateComment(reqDTO, commentId, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(parentComment.getContents()).isEqualTo(reqDTO.getContents());
            verify(commentRepository).save(parentComment);
        }

        @Test
        @DisplayName("[실패] 타인의 댓글 수정 시 예외 발생")
        void updateComment_ByNonAuthor_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer commentId = 1;
            CommentReqDTO reqDTO = CommentFixture.createUpdateReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            doNothing().when(validationUtil).validateUserRestricted(otherUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(parentComment);
            doThrow(new BadRequestExceptionMessage("해당 댓글의 작성자가 아닙니다"))
                    .when(validationUtil).validateUserIsAuthorOfComment(otherUser, parentComment);

            // when & then
            assertThatThrownBy(() -> commentUpdateService.updateComment(reqDTO, commentId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("작성자가 아닙니다");
        }
    }

    // ==================== 댓글 삭제 테스트 ====================

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentsTest {

        @Test
        @DisplayName("[성공] 작성자가 댓글 삭제 (답글 포함)")
        void deleteComments_ByAuthor_ShouldDeleteWithReplies() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;
            parentComment.setRepliesCount(2);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(parentComment);
            doNothing().when(validationUtil).validateUserIsAuthorOfComment(normalUser, parentComment);
            when(commentRepository.findByParentCommentCommentId(commentId)).thenReturn(List.of());
            doNothing().when(commentRepository).decreaseCommentCount(anyInt(), anyInt());

            // when
            int deletedReplies = commentUpdateService.deleteComments(commentId, userEmail);

            // then
            assertThat(deletedReplies).isZero();
            verify(commentRepository).delete(parentComment);
        }

        @Test
        @DisplayName("[실패] 타인의 댓글 삭제 시 예외 발생")
        void deleteComments_ByNonAuthor_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(parentComment);
            doThrow(new BadRequestExceptionMessage("해당 댓글의 작성자가 아닙니다"))
                    .when(validationUtil).validateUserIsAuthorOfComment(otherUser, parentComment);

            // when & then
            assertThatThrownBy(() -> commentUpdateService.deleteComments(commentId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("작성자가 아닙니다");

            verify(commentRepository, never()).delete(any(Comment.class));
        }
    }
}
