package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.dto.CommunityItemDTO;
import com.jandi.plan_backend.commu.community.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.util.CommunityUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommunityQueryService {
    private final ValidationUtil validationUtil;
    private final CommunityRepository communityRepository;
    private final ImageService imageService;
    private final CommunityUtil communityUtil;

    public CommunityQueryService(
            ValidationUtil validationUtil,
            CommunityRepository communityRepository,
            ImageService imageService,
            CommunityUtil communityUtil
    ) {
        this.validationUtil = validationUtil;
        this.communityRepository = communityRepository;
        this.imageService = imageService;
        this.communityUtil = communityUtil;
    }

    /** 특정 게시글 조회 */
    public Optional<CommunityItemDTO> getSpecPost(Integer postId, String userEmail) {
        //게시글의 존재 여부 검증
        Community community = validationUtil.validatePostExists(postId);
        community.setViewCount(community.getViewCount() + 1);
        communityRepository.save(community);

        //게시글 좋아요 여부
        boolean isLike = communityUtil.isLikedCommunity(userEmail, community);

        //게시글 반환
        Optional<Community> post = communityRepository.findByPostId(postId);
        return post.map(p -> new CommunityItemDTO(p, imageService, isLike)); // imageService 포함
    }

    /** 게시글 목록 전체 조회 */
    public Page<CommunityListDTO> getAllPosts(int page, int size) {
        long totalCount = communityRepository.count();
        Sort sort = Sort.by(Sort.Direction.DESC, "postId");
        return PaginationService.getPagedData(page, size, totalCount,
                (pageable) -> communityRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)),
                community -> {
                    String thumbnail = communityUtil.getThumbnailUrl(community);
                    return new CommunityListDTO(community, imageService, thumbnail);
                });
    }
}