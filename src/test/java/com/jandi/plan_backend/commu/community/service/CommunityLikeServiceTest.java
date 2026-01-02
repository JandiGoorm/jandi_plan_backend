package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.entity.CommunityLike;
import com.jandi.plan_backend.commu.community.repository.CommunityLikeRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
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
 * CommunityLikeService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 게시물 좋아요, 좋아요 취소
 * 테스트 기법: 동등분할 (유효/무효 입력), 경계값 분석
 */
@ExtendWith(MockitoExtension.class)
class CommunityLikeServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommunityLikeRepository communityLikeRepository;

    @Mock
    private CommunityRepository communityRepository;

    @InjectMocks
    private CommunityLikeService communityLikeService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private Community community;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createUserWithIdAndEmail(999, "other@example.com");
        community = CommunityFixture.createCommunity(otherUser); // 타인의 게시물
    }

    // ==================== 게시물 좋아요 테스트 ====================

    @Nested
    @DisplayName("게시물 좋아요")
    class LikePostTest {

        @Test
        @DisplayName("[성공] 타인의 게시물에 좋아요")
        void likePost_ToOthersPost_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(communityLikeRepository.existsByUserAndCommunity(normalUser, community)).thenReturn(false);
            when(communityLikeRepository.save(any(CommunityLike.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            communityLikeService.likePost(userEmail, postId);

            // then
            verify(communityLikeRepository).save(any(CommunityLike.class));
            verify(communityRepository).incrementLikeCount(postId);
        }

        @Test
        @DisplayName("[실패] 본인의 게시물에 좋아요 시 예외 발생")
        void likePost_ToOwnPost_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;
            Community myPost = CommunityFixture.createCommunity(normalUser); // 본인의 게시물

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(postId)).thenReturn(myPost);

            // when & then
            assertThatThrownBy(() -> communityLikeService.likePost(userEmail, postId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("본인의 게시글에 좋아요할 수 없습니다");

            verify(communityLikeRepository, never()).save(any(CommunityLike.class));
        }

        @Test
        @DisplayName("[실패] 이미 좋아요한 게시물에 중복 좋아요 시 예외 발생")
        void likePost_AlreadyLiked_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(communityLikeRepository.existsByUserAndCommunity(normalUser, community)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> communityLikeService.likePost(userEmail, postId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 좋아요한 게시물입니다");

            verify(communityLikeRepository, never()).save(any(CommunityLike.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 좋아요 시 예외 발생")
        void likePost_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> communityLikeService.likePost(userEmail, postId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");

            verify(communityLikeRepository, never()).save(any(CommunityLike.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 게시물에 좋아요 시 예외 발생")
        void likePost_ToNonExistentPost_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentPostId = 9999;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(nonExistentPostId))
                    .thenThrow(new BadRequestExceptionMessage("해당 게시글을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> communityLikeService.likePost(userEmail, nonExistentPostId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    // ==================== 게시물 좋아요 취소 테스트 ====================

    @Nested
    @DisplayName("게시물 좋아요 취소")
    class DeleteLikePostTest {

        @Test
        @DisplayName("[성공] 좋아요한 게시물 좋아요 취소")
        void deleteLikePost_WithExistingLike_ShouldSucceed() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;
            CommunityLike existingLike = new CommunityLike();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(communityLikeRepository.findByUserAndCommunity(normalUser, community))
                    .thenReturn(Optional.of(existingLike));

            // when
            communityLikeService.deleteLikePost(userEmail, postId);

            // then
            verify(communityLikeRepository).delete(existingLike);
            verify(communityRepository).decrementLikeCount(postId);
        }

        @Test
        @DisplayName("[실패] 좋아요하지 않은 게시물 좋아요 취소 시 예외 발생")
        void deleteLikePost_WithoutExistingLike_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(communityLikeRepository.findByUserAndCommunity(normalUser, community))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> communityLikeService.deleteLikePost(userEmail, postId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("좋아요한 적 없는 게시물입니다");

            verify(communityLikeRepository, never()).delete(any(CommunityLike.class));
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 좋아요 취소 시 예외 발생")
        void deleteLikePost_ByRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> communityLikeService.deleteLikePost(userEmail, postId))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("제한된 사용자");
        }
    }
}
