package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.dto.CommunityItemDTO;
import com.jandi.plan_backend.commu.community.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.fixture.CommunityFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.CommunityUtil;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommunityQueryService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 게시물 목록 조회, 게시물 상세 조회
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private CommunityUtil communityUtil;

    @InjectMocks
    private CommunityQueryService communityQueryService;

    private User normalUser;
    private Community community;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        community = CommunityFixture.createCommunity(normalUser);
    }

    // ==================== 게시물 목록 조회 테스트 ====================

    @Nested
    @DisplayName("게시물 목록 조회")
    class GetAllPostsTest {

        @Test
        @DisplayName("[성공] 페이지와 사이즈로 게시물 목록 조회")
        void getAllPosts_WithValidPageAndSize_ShouldReturnPagedList() {
            // given
            int page = 0;
            int size = 10;
            List<Community> communities = CommunityFixture.createCommunityList(normalUser, 3);
            Page<Community> communityPage = new PageImpl<>(communities);

            when(communityRepository.count()).thenReturn(3L);
            when(communityRepository.findAll(any(Pageable.class))).thenReturn(communityPage);
            when(communityUtil.getThumbnailUrl(any(Community.class))).thenReturn("https://example.com/thumbnail.jpg");

            // when
            Page<CommunityListDTO> result = communityQueryService.getAllPosts(page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            verify(communityRepository).count();
            verify(communityRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("[성공] 빈 목록일 때 빈 페이지 반환")
        void getAllPosts_WhenEmpty_ShouldReturnEmptyPage() {
            // given
            int page = 0;
            int size = 10;
            Page<Community> emptyPage = new PageImpl<>(List.of());

            when(communityRepository.count()).thenReturn(0L);
            when(communityRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // when
            Page<CommunityListDTO> result = communityQueryService.getAllPosts(page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ==================== 게시물 상세 조회 테스트 ====================

    @Nested
    @DisplayName("게시물 상세 조회")
    class GetSpecPostTest {

        @Test
        @DisplayName("[성공] 비로그인 사용자가 게시물 상세 조회")
        void getSpecPost_WithoutLogin_ShouldReturnPostWithoutLikeInfo() {
            // given
            Integer postId = 1;
            String userEmail = null;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            doNothing().when(communityRepository).incrementViewCount(postId);
            when(communityUtil.isLikedCommunity(userEmail, community)).thenReturn(false);

            // when
            CommunityItemDTO result = communityQueryService.getSpecPost(postId, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPostId()).isEqualTo(postId);
            assertThat(result.getTitle()).isEqualTo(community.getTitle());
            assertThat(result.getLiked()).isFalse();
            verify(communityRepository).incrementViewCount(postId);
        }

        @Test
        @DisplayName("[성공] 로그인 사용자가 좋아요한 게시물 조회 시 liked=true")
        void getSpecPost_WithLoginAndLiked_ShouldReturnLikedTrue() {
            // given
            Integer postId = 1;
            String userEmail = normalUser.getEmail();

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            doNothing().when(communityRepository).incrementViewCount(postId);
            when(communityUtil.isLikedCommunity(userEmail, community)).thenReturn(true);

            // when
            CommunityItemDTO result = communityQueryService.getSpecPost(postId, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getLiked()).isTrue();
        }

        @Test
        @DisplayName("[성공] 로그인 사용자가 좋아요하지 않은 게시물 조회 시 liked=false")
        void getSpecPost_WithLoginAndNotLiked_ShouldReturnLikedFalse() {
            // given
            Integer postId = 1;
            String userEmail = normalUser.getEmail();

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            doNothing().when(communityRepository).incrementViewCount(postId);
            when(communityUtil.isLikedCommunity(userEmail, community)).thenReturn(false);

            // when
            CommunityItemDTO result = communityQueryService.getSpecPost(postId, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getLiked()).isFalse();
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 게시물 조회 시 예외 발생")
        void getSpecPost_WithNonExistentPostId_ShouldThrowException() {
            // given
            Integer nonExistentPostId = 9999;
            String userEmail = null;

            when(validationUtil.validatePostExists(nonExistentPostId))
                    .thenThrow(new BadRequestExceptionMessage("해당 게시글을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> communityQueryService.getSpecPost(nonExistentPostId, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("해당 게시글을 찾을 수 없습니다");

            verify(communityRepository, never()).incrementViewCount(anyInt());
        }
    }
}
