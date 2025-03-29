package com.jandi.plan_backend.commu.community.service;

import com.jandi.plan_backend.commu.community.dto.CommunityListDTO;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityRepository;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.util.CommunityUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class CommunitySearchService {
    private final ValidationUtil validationUtil;
    private final ImageService imageService;
    private final CommunityRepository communityRepository;
    private final CommunityUtil communityUtil;

    public CommunitySearchService(
            ValidationUtil validationUtil,
            ImageService imageService,
            CommunityRepository communityRepository,
            CommunityUtil communityUtil
    ) {
        this.validationUtil = validationUtil;
        this.imageService = imageService;
        this.communityRepository = communityRepository;
        this.communityUtil = communityUtil;
    }

    @Transactional(readOnly = true)
    public Page<CommunityListDTO> search(String category, String keyword, int page, int size) {
        //category 예외 처리
        if(category == null || category.isEmpty()) {
            throw new BadRequestExceptionMessage("카테고리를 입력하세요");
        }

        //keyword 예외 처리
        if(keyword == null || keyword.isEmpty()){
            throw new BadRequestExceptionMessage("검색어를 입력하세요.");
        }else if(keyword.trim().length() < 2){
            throw new BadRequestExceptionMessage("검색어는 2글자 이상이어야 합니다");
        }

        // 검색
        List<Community> searchList = switch (category) {
            case "TITLE" -> // 제목 검색
                    communityRepository.searchAllByTitleContaining(keyword);
            case "CONTENT" -> // 내용 검색
                    communityRepository.searchAllByContentsContaining(keyword);
            case "BOTH" -> // 제목 + 내용 검색
                    communityRepository.searchByTitleAndContents("\"" + keyword + "\""); //공백 포함하여 계산되도록 따옴표로 래핑
            case "HASHTAG" -> { // 해시태그 검색
                validationUtil.validateIsHashTagValid(keyword); //키워드가 해시태그대로 들어왔는지 검증
                yield communityRepository.searchByHashTag("\"" + keyword + "\"");
            }
            default ->
                    throw new IllegalStateException("카테고리 지정이 잘못되었습니다: " + category);
        };
        searchList.sort(Comparator.comparing(Community::getPostId).reversed()); // postId 내림차순으로 정렬
        long totalCount = searchList.size();

        return PaginationService.getPagedData(page, size, totalCount,
                (pageable) -> {
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), searchList.size());
                    List<Community> pagedList = searchList.subList(start, end);
                    return new PageImpl<>(pagedList, pageable, totalCount);
                },
                community -> {
                    String thumbnail = communityUtil.getThumbnailUrl(community);
                    return new CommunityListDTO(community, imageService, thumbnail);
                }
        );
    }

}
