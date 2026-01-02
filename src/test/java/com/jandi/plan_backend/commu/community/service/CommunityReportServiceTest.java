package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.dto.ReportReqDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.entity.CommunityReported;
import com.jandi.plan_backend.commu.community.repository.CommunityReportedRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommunityReportService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 게시물 신고
 * 테스트 기법: 동등분할 (유효/무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class CommunityReportServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommunityReportedRepository communityReportedRepository;

    @InjectMocks
    private CommunityReportService communityReportService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private Community community;
    private ReportReqDTO reportReqDTO;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        community = CommunityFixture.createCommunity(otherUser);
        reportReqDTO = new ReportReqDTO();
        ReflectionTestUtils.setField(reportReqDTO, "contents", "부적절한 내용");
    }

    // ==================== 게시물 신고 테스트 ====================

    @Nested
    @DisplayName("게시물 신고")
    class ReportPostTest {

        @Test
        @DisplayName("[성공] 타인의 게시물 신고")
        void reportPost_ToOthersPost_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(communityReportedRepository.existsByUserAndCommunity(normalUser, community)).thenReturn(false);
            when(communityReportedRepository.save(any(CommunityReported.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            communityReportService.reportPost(userEmail, postId, reportReqDTO);

            // then
            verify(communityReportedRepository).save(any(CommunityReported.class));
        }

        @Test
        @DisplayName("[실패] 이미 신고한 게시물 중복 신고 시 예외 발생")
        void reportPost_AlreadyReported_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(communityReportedRepository.existsByUserAndCommunity(normalUser, community)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> communityReportService.reportPost(userEmail, postId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 신고한 게시글");

            verify(communityReportedRepository, never()).save(any(CommunityReported.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 신고 시 예외 발생")
        void reportPost_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> communityReportService.reportPost(userEmail, postId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");

            verify(communityReportedRepository, never()).save(any(CommunityReported.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 게시물 신고 시 예외 발생")
        void reportPost_ToNonExistentPost_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentPostId = 9999;

            when(validationUtil.validatePostExists(nonExistentPostId))
                    .thenThrow(new BadRequestExceptionMessage("존재하지 않는 게시글입니다"));

            // when & then
            assertThatThrownBy(() -> communityReportService.reportPost(userEmail, nonExistentPostId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("존재하지 않는 게시글");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 사용자가 신고 시 예외 발생")
        void reportPost_ByNonExistentUser_ShouldThrowException() {
            // given
            String nonExistentEmail = "nonexistent@example.com";
            Integer postId = 1;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(validationUtil.validateUserExists(nonExistentEmail))
                    .thenThrow(new BadRequestExceptionMessage("존재하지 않는 사용자입니다"));

            // when & then
            assertThatThrownBy(() -> communityReportService.reportPost(nonExistentEmail, postId, reportReqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("존재하지 않는 사용자");
        }
    }
}
