package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.repository.CommentRepository;
import com.jandi.plan_backend.commu.community.dto.RepliesDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.fixture.CommentFixture;
import com.jandi.plan_backend.fixture.CommunityFixture;
import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.CommentUtil;
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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentQueryService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 댓글 목록 조회, 답글 목록 조회
 * 테스트 원칙: 동등 분할 (유효 입력 / 무효 입력)
 */
@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentUtil commentUtil;

    @InjectMocks
    private CommentQueryService commentQueryService;

    private User normalUser;
    private Community community;
    private Comment parentComment;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        community = CommunityFixture.createCommunity(normalUser);
        parentComment = CommentFixture.createComment(normalUser, community);
    }

    // ==================== 댓글 목록 조회 테스트 ====================

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetAllCommentsTest {

        @Test
        @DisplayName("[성공] 게시물의 댓글 목록 조회")
        void getAllComments_WithValidPostId_ShouldReturnCommentList() {
            // given
            Integer postId = 1;
            int page = 0;
            int size = 10;
            String userEmail = null;
            List<Comment> comments = CommentFixture.createCommentList(normalUser, community, 3);

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId)).thenReturn(3L);
            when(commentRepository.findByCommunityPostIdAndParentCommentIsNull(eq(postId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(comments));
            when(commentUtil.getCommentUsersMap(anyList())).thenReturn(Map.of(normalUser.getUserId(), normalUser));
            when(commentUtil.getLikedCommentIds(anyList(), any())).thenReturn(Set.of());

            // when
            Page<ParentCommentDTO> result = commentQueryService.getAllComments(postId, page, size, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            verify(validationUtil).validatePostExists(postId);
        }

        @Test
        @DisplayName("[성공] 댓글이 없는 게시물 조회 시 빈 목록 반환")
        void getAllComments_WhenNoComments_ShouldReturnEmptyPage() {
            // given
            Integer postId = 1;
            int page = 0;
            int size = 10;
            String userEmail = null;

            when(validationUtil.validatePostExists(postId)).thenReturn(community);
            when(commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId)).thenReturn(0L);
            when(commentRepository.findByCommunityPostIdAndParentCommentIsNull(eq(postId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(commentUtil.getCommentUsersMap(anyList())).thenReturn(Map.of());
            when(commentUtil.getLikedCommentIds(anyList(), any())).thenReturn(Set.of());

            // when
            Page<ParentCommentDTO> result = commentQueryService.getAllComments(postId, page, size, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 게시물의 댓글 조회 시 예외 발생")
        void getAllComments_WithNonExistentPostId_ShouldThrowException() {
            // given
            Integer nonExistentPostId = 9999;
            int page = 0;
            int size = 10;
            String userEmail = null;

            doThrow(new BadRequestExceptionMessage("해당 게시글을 찾을 수 없습니다"))
                    .when(validationUtil).validatePostExists(nonExistentPostId);

            // when & then
            assertThatThrownBy(() -> commentQueryService.getAllComments(nonExistentPostId, page, size, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("해당 게시글을 찾을 수 없습니다");
        }
    }

    // ==================== 답글 목록 조회 테스트 ====================

    @Nested
    @DisplayName("답글 목록 조회")
    class GetAllRepliesTest {

        @Test
        @DisplayName("[성공] 댓글의 답글 목록 조회")
        void getAllReplies_WithValidCommentId_ShouldReturnReplyList() {
            // given
            Integer commentId = 1;
            int page = 0;
            int size = 10;
            String userEmail = null;
            Comment reply = CommentFixture.createReply(normalUser, community, parentComment);
            List<Comment> replies = List.of(reply);

            when(validationUtil.validateCommentExists(commentId)).thenReturn(parentComment);
            when(commentRepository.countByParentCommentCommentId(commentId)).thenReturn(1L);
            when(commentRepository.findByParentCommentCommentId(eq(commentId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(replies));
            when(commentUtil.getCommentUsersMap(anyList())).thenReturn(Map.of(normalUser.getUserId(), normalUser));
            when(commentUtil.getLikedCommentIds(anyList(), any())).thenReturn(Set.of());

            // when
            Page<RepliesDTO> result = commentQueryService.getAllReplies(commentId, page, size, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(validationUtil).validateCommentExists(commentId);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 댓글의 답글 조회 시 예외 발생")
        void getAllReplies_WithNonExistentCommentId_ShouldThrowException() {
            // given
            Integer nonExistentCommentId = 9999;
            int page = 0;
            int size = 10;
            String userEmail = null;

            doThrow(new BadRequestExceptionMessage("해당 댓글을 찾을 수 없습니다"))
                    .when(validationUtil).validateCommentExists(nonExistentCommentId);

            // when & then
            assertThatThrownBy(() -> commentQueryService.getAllReplies(nonExistentCommentId, page, size, userEmail))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("해당 댓글을 찾을 수 없습니다");
        }
    }
}
