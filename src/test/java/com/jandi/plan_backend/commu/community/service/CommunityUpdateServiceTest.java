package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.comment.repository.CommentLikeRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.dto.CommunityReqDTO;
import com.jandi.plan_backend.commu.community.dto.CommunityRespDTO;
import com.jandi.plan_backend.commu.community.dto.PostFinalizeReqDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityLikeRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityReportedRepository;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.fixture.CommunityFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.image.service.InMemoryTempPostService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.CommunityUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommunityUpdateService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 게시물 생성, 수정, 삭제
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class CommunityUpdateServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private InMemoryTempPostService inMemoryTempPostService;

    @Mock
    private CommunityUtil communityUtil;

    @Mock
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private CommunityReportedRepository communityReportedRepository;

    @Mock
    private CommunityLikeRepository communityLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentReportedRepository commentReportedRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private ImageCleanupService imageCleanupService;

    @InjectMocks
    private CommunityUpdateService communityUpdateService;

    private User normalUser;
    private User restrictedUser;
    private User otherUser;
    private Community community;

    @BeforeEach
    void setUp() {
        // 트랜잭션 동기화 매니저 초기화 (단위 테스트용)
        TransactionSynchronizationManager.initSynchronization();
        
        normalUser = UserFixture.createNormalUser();
        restrictedUser = UserFixture.createRestrictedUser();
        otherUser = UserFixture.createNormalUser();
        otherUser.setUserId(999);
        otherUser.setEmail("other@example.com");
        
        community = CommunityFixture.createCommunity(normalUser);
    }

    @AfterEach
    void tearDown() {
        // 트랜잭션 동기화 매니저 정리
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // ==================== 게시물 생성 테스트 ====================

    @Nested
    @DisplayName("게시물 생성")
    class FinalizePostTest {

        @Test
        @DisplayName("[성공] 유효한 요청으로 게시물 생성")
        void finalizePost_WithValidRequest_ShouldCreatePost() {
            // given
            String userEmail = normalUser.getEmail();
            int tempPostId = -1;
            PostFinalizeReqDTO reqDTO = CommunityFixture.createFinalizeReqDTO(tempPostId);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            doNothing().when(validationUtil).validateIsHashtagListValid(anyList());
            doNothing().when(inMemoryTempPostService).validateTempId(tempPostId, normalUser.getUserId());
            when(communityUtil.getPreview(anyString())).thenReturn("미리보기 내용");
            when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
                Community saved = invocation.getArgument(0);
                saved.setPostId(1);
                return saved;
            });
            doNothing().when(imageService).updateTargetId(anyString(), anyInt(), anyInt());
            doNothing().when(inMemoryTempPostService).removeTempId(tempPostId);

            // when
            CommunityRespDTO result = communityUpdateService.finalizePost(userEmail, reqDTO);

            // then
            assertThat(result).isNotNull();
            verify(communityRepository).save(any(Community.class));
            verify(imageService).updateTargetId("community", tempPostId, 1);
            verify(inMemoryTempPostService).removeTempId(tempPostId);
        }

        @Test
        @DisplayName("[실패] 제한된 사용자가 게시물 생성 시 예외 발생")
        void finalizePost_WithRestrictedUser_ShouldThrowException() {
            // given
            String userEmail = restrictedUser.getEmail();
            PostFinalizeReqDTO reqDTO = CommunityFixture.createFinalizeReqDTO(-1);

            when(validationUtil.validateUserExists(userEmail)).thenReturn(restrictedUser);
            doThrow(new BadRequestExceptionMessage("활동이 제한된 사용자입니다"))
                    .when(validationUtil).validateUserRestricted(restrictedUser);

            // when & then
            assertThatThrownBy(() -> communityUpdateService.finalizePost(userEmail, reqDTO))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("활동이 제한된 사용자");

            verify(communityRepository, never()).save(any(Community.class));
        }
    }

    // ==================== 게시물 수정 테스트 ====================

    @Nested
    @DisplayName("게시물 수정")
    class UpdatePostTest {

        @Test
        @DisplayName("[성공] 작성자가 게시물 수정")
        void updatePost_ByAuthor_ShouldUpdateSuccessfully() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;
            CommunityReqDTO reqDTO = CommunityFixture.createUpdateReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            doNothing().when(validationUtil).validateIsHashtagListValid(anyList());
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            doNothing().when(validationUtil).validateUserIsAuthorOfPost(normalUser, community);
            when(communityUtil.getPreview(anyString())).thenReturn("수정된 미리보기");

            // when
            CommunityRespDTO result = communityUpdateService.updatePost(reqDTO, postId, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(community.getTitle()).isEqualTo(reqDTO.getTitle());
            assertThat(community.getContents()).isEqualTo(reqDTO.getContent());
        }

        @Test
        @DisplayName("[실패] 타인의 게시물 수정 시 예외 발생")
        void updatePost_ByNonAuthor_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer postId = 1;
            CommunityReqDTO reqDTO = CommunityFixture.createUpdateReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            doNothing().when(validationUtil).validateUserRestricted(otherUser);
            doNothing().when(validationUtil).validateIsHashtagListValid(anyList());
            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            doThrow(new BadRequestExceptionMessage("해당 글의 작성자가 아닙니다"))
                    .when(validationUtil).validateUserIsAuthorOfPost(otherUser, community);

            // when & then
            assertThatThrownBy(() -> communityUpdateService.updatePost(reqDTO, postId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("작성자가 아닙니다");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 게시물 수정 시 예외 발생")
        void updatePost_WithNonExistentPost_ShouldThrowException() {
            // given
            String userEmail = normalUser.getEmail();
            Integer nonExistentPostId = 9999;
            CommunityReqDTO reqDTO = CommunityFixture.createUpdateReqDTO();

            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            doNothing().when(validationUtil).validateIsHashtagListValid(anyList());
            when(validationUtil.validatePostExists(nonExistentPostId))
                    .thenThrow(new BadRequestExceptionMessage("해당 게시글을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> communityUpdateService.updatePost(reqDTO, nonExistentPostId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("해당 게시글을 찾을 수 없습니다");
        }
    }

    // ==================== 게시물 삭제 테스트 ====================

    @Nested
    @DisplayName("게시물 삭제")
    class DeletePostTest {

        @Test
        @DisplayName("[성공] 작성자가 게시물 삭제")
        void deletePost_ByAuthor_ShouldDeleteSuccessfully() {
            // given
            String userEmail = normalUser.getEmail();
            Integer postId = 1;
            community.setCommentCount(5);

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserIsAuthorOfPost(normalUser, community);
            when(communityReportedRepository.findByCommunity_PostId(postId)).thenReturn(List.of());
            when(communityLikeRepository.findByCommunity(community)).thenReturn(List.of());
            when(commentRepository.findByCommunity(community)).thenReturn(List.of());
            when(imageRepository.findAllByTargetTypeAndTargetId("community", postId)).thenReturn(List.of());

            // when
            int deletedComments = communityUpdateService.deletePost(postId, userEmail);

            // then
            assertThat(deletedComments).isEqualTo(5);
            verify(communityRepository).delete(community);
        }

        @Test
        @DisplayName("[실패] 타인의 게시물 삭제 시 예외 발생")
        void deletePost_ByNonAuthor_ShouldThrowException() {
            // given
            String userEmail = otherUser.getEmail();
            Integer postId = 1;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(validationUtil.validateUserExists(userEmail)).thenReturn(otherUser);
            doThrow(new BadRequestExceptionMessage("해당 글의 작성자가 아닙니다"))
                    .when(validationUtil).validateUserIsAuthorOfPost(otherUser, community);

            // when & then
            assertThatThrownBy(() -> communityUpdateService.deletePost(postId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("작성자가 아닙니다");

            verify(communityRepository, never()).delete(any(Community.class));
        }
    }
}
