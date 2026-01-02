package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.entity.CommentReported;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.community.dto.ReportReqDTO;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentReportService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 댓글 신고
 * 테스트 기법: 동등분할 (유효/무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class CommentReportServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommentReportedRepository commentReportedRepository;

    @InjectMocks
    private CommentReportService commentReportService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private Community community;
    private Comment comment;
    private ReportReqDTO reportReqDTO;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        community = CommunityFixture.createCommunity(otherUser);
        comment = CommentFixture.createComment(otherUser, community);
        reportReqDTO = new ReportReqDTO();
        ReflectionTestUtils.setField(reportReqDTO, "contents", "부적절한 내용");
    }

    // ==================== 댓글 신고 테스트 ====================

    @Nested
    @DisplayName("댓글 신고")
    class ReportCommentTest {

        @Test
        @DisplayName("[성공] 타인의 댓글 신고")
        void reportComment_ToOthersComment_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(comment);
            when(commentReportedRepository.findByUser_userIdAndComment_CommentId(normalUser.getUserId(), commentId)).thenReturn(Optional.empty());
            when(commentReportedRepository.save(any(CommentReported.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            commentReportService.reportComment(userEmail, commentId, reportReqDTO);

            // then
            verify(commentReportedRepository).save(any(CommentReported.class));
        }

        @Test
        @DisplayName("[실패] 이미 신고한 댓글 중복 신고 시 예외 발생")
        void reportComment_AlreadyReported_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(commentId)).thenReturn(comment);
            when(commentReportedRepository.findByUser_userIdAndComment_CommentId(normalUser.getUserId(), commentId)).thenReturn(Optional.of(new CommentReported()));

            // when & then
            assertThatThrownBy(() -> commentReportService.reportComment(userEmail, commentId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 신고한 댓글입니다");

            verify(commentReportedRepository, never()).save(any(CommentReported.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 신고 시 예외 발생")
        void reportComment_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer commentId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> commentReportService.reportComment(userEmail, commentId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");

            verify(commentReportedRepository, never()).save(any(CommentReported.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 댓글 신고 시 예외 발생")
        void reportComment_ToNonExistentComment_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentCommentId = 9999;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validateCommentExists(nonExistentCommentId))
                    .thenThrow(new BadRequestExceptionMessage("해당 댓글을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> commentReportService.reportComment(userEmail, nonExistentCommentId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자가 신고 시 예외 발생")
        void reportComment_ByNonExistentUser_ShouldThrowException() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            Integer commentId = 1;

            when(validationUtil.validateUserExists(nonExistentEmail))
                    .thenThrow(new BadRequestExceptionMessage("사용자를 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> commentReportService.reportComment(nonExistentEmail, commentId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }
}
