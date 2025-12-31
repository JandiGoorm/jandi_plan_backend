package com.jandi.plan_backend.fixture;

import com.jandi.plan_backend.commu.community.dto.CommunityReqDTO;
import com.jandi.plan_backend.commu.community.dto.PostFinalizeReqDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Community 관련 테스트 데이터 팩토리 클래스
 */
public class CommunityFixture {

    private static final LocalDateTime NOW = TimeUtil.now();

    /**
     * 기본 게시물 생성
     */
    public static Community createCommunity(User user) {
        Community community = new Community();
        community.setPostId(1);
        community.setUser(user);
        community.setTitle("테스트 게시물 제목");
        community.setContents("테스트 게시물 내용입니다.");
        community.setPreview("테스트 게시물 내용입니다.");
        community.setHashtags(Arrays.asList("여행", "유럽"));
        community.setLikeCount(0);
        community.setCommentCount(0);
        community.setViewCount(0);
        community.setCreatedAt(NOW);
        return community;
    }

    /**
     * 특정 ID를 가진 게시물 생성
     */
    public static Community createCommunityWithId(Integer postId, User user) {
        Community community = createCommunity(user);
        community.setPostId(postId);
        community.setTitle("테스트 게시물 " + postId);
        return community;
    }

    /**
     * 좋아요가 있는 게시물 생성
     */
    public static Community createCommunityWithLikes(User user, int likeCount) {
        Community community = createCommunity(user);
        community.setLikeCount(likeCount);
        return community;
    }

    /**
     * 댓글이 있는 게시물 생성
     */
    public static Community createCommunityWithComments(User user, int commentCount) {
        Community community = createCommunity(user);
        community.setCommentCount(commentCount);
        return community;
    }

    /**
     * 게시물 생성 요청 DTO
     */
    public static PostFinalizeReqDTO createFinalizeReqDTO(int tempPostId) {
        PostFinalizeReqDTO dto = new PostFinalizeReqDTO();
        dto.setTempPostId(tempPostId);
        dto.setTitle("새 게시물 제목");
        dto.setContent("새 게시물 내용입니다.");
        dto.setHashtag(Arrays.asList("여행", "서울"));
        return dto;
    }

    /**
     * 게시물 수정 요청 DTO (final 필드이므로 생성자 사용)
     */
    public static CommunityReqDTO createUpdateReqDTO() {
        return new CommunityReqDTO(
                "수정된 게시물 제목",
                "수정된 게시물 내용입니다.",
                Arrays.asList("여행", "부산")
        );
    }

    /**
     * 여러 게시물 목록 생성
     */
    public static List<Community> createCommunityList(User user, int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> createCommunityWithId(i, user))
                .toList();
    }
}
