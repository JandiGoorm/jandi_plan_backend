package com.jandi.plan_backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.commu.community.repository.CommunityLikeRepository;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommunityUtil {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final CommunityLikeRepository communityLikeRepository;

    @Value("${image-prefix}")
    private String prefix;

    public CommunityUtil(
            UserRepository userRepository,
            ImageRepository imageRepository,
            ImageService imageService,
            CommunityLikeRepository communityLikeRepository) {
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
        this.communityLikeRepository = communityLikeRepository;
    }


    public boolean isLikedCommunity(String userEmail, Community community) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        return user != null && communityLikeRepository.findByCommunityAndUser(community, user).isPresent();
    }

    public String getThumbnailUrl(Community community) {
        List<Image> thumbnails = imageRepository.findAllByTargetTypeAndTargetId("community", community.getPostId());
        return (thumbnails.isEmpty()) ? "" : prefix + thumbnails.get(0).getImageUrl();
    }

    /** 엔터는 ""로 치환 후, 최대 200자 길이의 미리보기 내용을 추출합니다. */
    public String getPreview(String contents) {
        StringBuilder preview = new StringBuilder();
        try {
            // contents를 ObjectMapper로 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(contents);

            // contents 의 구조: {"ops":[{"insert":"..."},{"attributes":{"code-block":"plain"},..}
            // 여기서 실제 추출해야 할 부분은 insert의 값이므로, for문을 돌면서 글자수가 채워질 때까지 insert 안의 내용을 순차적으로 더해갑니다
            for(JsonNode opsNode : node.get("ops")) {
                // 남은 preview의 길이 계산해서 공간 없다면
                int maxPreviewLength = 200;
                int leftLength = maxPreviewLength - preview.length();
                if(leftLength < 1) break;

                JsonNode insertNode = opsNode.get("insert");
                if(insertNode != null) {
                    //원문에서 \n은 ""으로 치환
                    String insertNodeText = insertNode.asText().replace("\n", "");
                    preview.append((insertNodeText.length() > leftLength) ? //넘치면 잘라내기
                            insertNodeText.substring(0, leftLength) : insertNodeText);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return preview.toString();
    }

    /**
     * 게시글 내용에서 실제 사용 중인 이미지 파일명을 추출합니다.
     * 예: "https://storage.googleapis.com/plan-storage/encodedFileName.jpg"에서 "encodedFileName.jpg" 추출
     */
    public Set<String> extractImageFileNamesFromContent(String content) {
        Set<String> fileNames = new HashSet<>();
        Pattern pattern = Pattern.compile(prefix + "([^\"\\s]+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            fileNames.add(matcher.group(1));
        }
        return fileNames;
    }

    /**
     * 게시글과 연결된 이미지 중, 게시글 내용에 포함되지 않은 이미지를 삭제합니다.
     */
    public void cleanupUnusedImages(Community post) {
        Set<String> usedFileNames = extractImageFileNamesFromContent(post.getContents());
        List<Image> images = imageRepository.findAllByTargetTypeAndTargetId("community", post.getPostId());
        for (Image image : images) {
            if (!usedFileNames.contains(image.getImageUrl())) {
                imageService.deleteImage(image.getImageId());
            }
        }
    }
}
