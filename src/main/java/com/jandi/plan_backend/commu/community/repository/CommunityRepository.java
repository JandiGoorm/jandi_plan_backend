package com.jandi.plan_backend.commu.community.repository;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {
    Optional<Community> findByPostId(Integer postId);

    /** 검사 */
    // 제목
    List<Community> searchAllByTitleContaining(String keyword); // 내부적으로 like 연산자 이용

    // 내용
    List<Community> searchAllByContentsContaining(String keyword); // 내부적으로 like 연산자 이용

    // 제목 + 내용
    // title과 contents 중 일부가 keyword에 매칭되는지 검사. like보다 속도가 빠른 fulltext 인덱싱 방식 채택
    // nativeQuery = true로 하여 단어 일부만 검색해도 매칭되도록 함 (단어: 띄어쓰기 기준)
    @Query(value = "SELECT * FROM community WHERE MATCH(title, contents) AGAINST(:keyword IN BOOLEAN MODE)", nativeQuery = true)
    List<Community> searchByTitleAndContents(String keyword);

    List<Community> findByUser(User user);

    //해시태그로 검색: JSON 형태로 검색
    @Query(value = "SELECT * FROM community WHERE JSON_CONTAINS(hashtags, :jsonTag)", nativeQuery = true)
    List<Community> searchByHashTag(@Param("jsonTag") String keyword);

    // 증감 쿼리
    @Modifying
    @Query("update Community c set c.likeCount = c.likeCount + 1 where c.postId = :id")
    void incrementLikeCount(@Param("id") Integer id);

    @Modifying
    @Query("update Community c set c.likeCount = c.likeCount - 1 where c.postId = :id")
    void decrementLikeCount(@Param("id") Integer id);

    @Modifying
    @Query("update Community c set c.viewCount = c.viewCount + 1 where c.postId = :id")
    void incrementViewCount(@Param("id") Integer id);

}
