package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.*;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommentRepository;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.jandi.plan_backend.util.service.PaginationService;

import java.time.LocalDateTime;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;


    // 생성자를 통해 필요한 의존성들을 주입받음.
    public CommunityService(CommunityRepository communityRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
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

    /**
     * 게시글 작성
     */
    public CommunityWriteRespDTO writePost(CommunityWritePostDTO postDTO, String userEmail) {
        // 사용자 관련 오류 처리
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        if(user.getReported()){
            throw new RuntimeException("비정상적인 활동이 반복되어 게시글 작성이 제한되었습니다.");
        }

        // Community 엔티티 생성 및 저장
        Community community = new Community();
        community.setUser(user);
        community.setTitle(postDTO.getTitle());
        community.setContents(postDTO.getContent());
        community.setCreatedAt(LocalDateTime.now());
        community.setLikeCount(0);
        community.setCommentCount(0);
        communityRepository.save(community);

        // DB 저장
        return new CommunityWriteRespDTO(community);
    }
}
