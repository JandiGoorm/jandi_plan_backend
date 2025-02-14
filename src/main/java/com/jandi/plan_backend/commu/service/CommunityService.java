package com.jandi.plan_backend.commu.service;

import com.jandi.plan_backend.commu.dto.CommunityDTO;
import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.commu.repository.CommunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;

    // 생성자를 통해 필요한 의존성들을 주입받음.
    public CommunityService(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    /** 페이지 단위로 게시물 리스트 조회 */
    public Page<CommunityDTO> getAllPosts(int page, int size) {

        //페이지 범위 오류 처리
        long totalCount = communityRepository.count(); // 전체 게시물 개수
        int totalPages = (int)((double)totalCount / size); // 전체 페이지 수
        if(page < 0 || page > totalPages) {
            throw new RuntimeException("잘못된 페이지 번호 요청");
        }

        Pageable pageable = PageRequest.of(page, size);  // 페이지네이션 적용
        Page<Community> postsPage = communityRepository.findAll(pageable);

        return postsPage.map(CommunityDTO::new);
    }
}
