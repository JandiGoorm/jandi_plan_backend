package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;

    // 생성자를 통해 필요한 의존성들을 주입받음.
    public CommunityService(CommunityRepository communityRepository, CommentRepository commentRepository) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
    }

    /** 페이지 단위로 게시물 리스트 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {

        //페이지 범위 오류 처리
        long totalCount = communityRepository.count(); // 전체 게시물 개수
        int totalPages = (int)((double)totalCount / size); // 전체 페이지 수
        if(page < 0 || page > totalPages) {
            throw new RuntimeException("잘못된 페이지 번호 요청");
        }

        Pageable pageable = PageRequest.of(page, size);  // 페이지네이션 적용
        Page<Community> postsPage = communityRepository.findAll(pageable);

        return postsPage.map(CommunityListDTO::new);
    }

    /** 특정 게시물의 부모 댓글(최상위 댓글)만 조회 */
    public Page<ParentCommentDTO> getParentComments(Integer postId, int page, int size) {
        //postId 관련 오류 처리
        if(postId == null) {
            throw new RuntimeException("postId를 지정하지 않았습니다");
        }else if(!communityRepository.existsById(postId)) {
            throw new RuntimeException("존재하지 않는 게시물입니다");
        }

        //page 관련 오류 처리
        if(page < 0) {
            throw new RuntimeException("잘못된 페이지 번호 요청");
        }

        Pageable pageable = PageRequest.of(page, size); //페이지네이션 적용
        Page<Comments> parentCommentsPage = commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable);

        return parentCommentsPage.map(ParentCommentDTO::new);
    }
}
