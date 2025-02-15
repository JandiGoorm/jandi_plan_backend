package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.CommunityItemDTO;
import com.jandi.plan_backend.commu.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.dto.repliesDTO;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.jandi.plan_backend.util.service.PaginationService;

import java.util.Optional;

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
        long totalCount = communityRepository.count();
        return PaginationService.getPagedData(page, size, totalCount, communityRepository::findAll, CommunityListDTO::new);
    }

    /** 특정 게시물의 정보 조회 */
    public CommunityItemDTO getPostItem(Integer postId) {
        if (postId == null) {
            throw new IllegalArgumentException("postId를 지정하지 않았습니다.");
        }

        return communityRepository.findByPostId(postId)
                .map(CommunityItemDTO::new)
                .orElseThrow(() -> new RuntimeException("해당 게시글이 존재하지 않습니다."));
    }


    /** 특정 게시물의 부모 댓글(최상위 댓글)만 조회 */
    public Page<ParentCommentDTO> getParentComments(Integer postId, int page, int size) {
        //postId 관련 오류 처리
        if (postId == null) {
            throw new RuntimeException("postId를 지정하지 않았습니다.");
        }else if(!communityRepository.existsById(postId)){
            throw new RuntimeException("존재하지 않는 게시물입니다.");
        }

        long totalCount = commentRepository.countByCommunityPostIdAndParentCommentIsNull(postId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
                ParentCommentDTO::new);
    }

    /** 특정 댓글의 답글만 조회 */
    public Page<repliesDTO> getReplies(Integer parentCommentId, int page, int size) {
        //commentId 관련 오류 처리
        if (parentCommentId == null) {
            throw new RuntimeException("parentCommentId를 지정하지 않았습니다.");
        }else if(!communityRepository.existsById(parentCommentId)){
            throw new RuntimeException("존재하지 않는 댓글입니다.");
        }

        long totalCount = commentRepository.countByParentCommentCommentId(parentCommentId);
        return PaginationService.getPagedData(page, size, totalCount,
                pageable -> commentRepository.findByParentCommentCommentId(parentCommentId, pageable),
                repliesDTO::new);
    }
}
