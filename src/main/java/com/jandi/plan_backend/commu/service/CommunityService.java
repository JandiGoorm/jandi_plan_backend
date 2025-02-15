package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.ParentCommentDTO;
import com.jandi.plan_backend.commu.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.jandi.plan_backend.util.service.PaginationService;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;

    // CommunityRepository와 CommentRepository를 생성자 주입받음.
    public CommunityService(CommunityRepository communityRepository, CommentRepository commentRepository) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
    }

    /** 페이지 단위로 게시물 리스트 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();
        return PaginationService.getPagedData(page, size, totalCount, communityRepository::findAll, CommunityListDTO::new);
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
}