package com.jandi.plan_backend.openai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jandi.plan_backend.openai.dto.AiRestaurantDTO;
import com.jandi.plan_backend.openai.dto.OpenAiResponseDTO;
import com.jandi.plan_backend.openai.entity.AiInteraction;
import com.jandi.plan_backend.openai.entity.AiRestaurant;
import com.jandi.plan_backend.openai.repository.AiInteractionRepository;
import com.jandi.plan_backend.openai.repository.AiRestaurantRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRestaurantService {

    private final CityRepository cityRepository;
    private final AiInteractionRepository aiInteractionRepository;
    private final AiRestaurantRepository aiRestaurantRepository;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 파싱 실패 시 몇 번 재시도할지
    private static final int RETRY_COUNT = 3;

    /**
     * 도시별 맛집 정보 업데이트 로직
     * 예: 스케줄러나 수동 호출로 실행
     */
    public void updateAllCitiesRestaurants() {
        List<City> allCities = cityRepository.findAll();
        for (City city : allCities) {
            updateRestaurantsForCity(city);
        }
    }

    /**
     * 특정 도시의 맛집 정보를 AI에 질의 → DB에 저장
     */
    public void updateRestaurantsForCity(City city) {
        String prompt = buildPromptForCity(city);
        String rawJson = null;
        AiInteraction interaction = null;

        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
            try {
                // 1) AI 질의
                OpenAiResponseDTO response = openAiService.askChatCompletion(prompt, 512);

                // 2) 응답에서 JSON 추출
                rawJson = extractContentFromChat(response);

                // 3) ai_interaction 테이블에 기록
                interaction = new AiInteraction();
                interaction.setQueryType("restaurant");
                interaction.setPrompt(prompt);
                interaction.setRawResponse(rawJson);
                aiInteractionRepository.save(interaction);

                // 4) JSON 파싱 (가정: JSON 배열 형태)
                List<RestaurantItem> items = objectMapper.readValue(rawJson, new TypeReference<>() {});

                // 5) 저장
                for (RestaurantItem item : items) {
                    AiRestaurant restaurant = new AiRestaurant();
                    restaurant.setInteraction(interaction);
                    restaurant.setCityId(city.getCityId());
                    restaurant.setName(item.getName());
                    restaurant.setLatitude(item.getLatitude());
                    restaurant.setLongitude(item.getLongitude());
                    restaurant.setRating(item.getRating());
                    restaurant.setDescription(item.getDescription());
                    restaurant.setImageUrl(item.getImage_url());
                    aiRestaurantRepository.save(restaurant);
                }

                log.info("도시 {} ({})에 대해 {}개의 맛집 정보를 저장했습니다. (attempt={})",
                        city.getName(), city.getCityId(), items.size(), attempt);

                // 파싱이 정상적으로 끝났으면 재시도 루프 종료
                break;

            } catch (Exception e) {
                log.error("도시 {} 맛집 업데이트 중 오류 (attempt={}): {}", city.getName(), attempt, e.getMessage());

                // 마지막 시도라면 그대로 예외 처리
                if (attempt == RETRY_COUNT) {
                    log.error("최대 재시도 횟수를 초과했습니다. 도시={}, rawJson={}", city.getName(), rawJson);
                } else {
                    // 재시도 로직: prompt 변경 등 가능
                    // prompt += "\n이전 JSON이 유효하지 않습니다. 다시 완전한 JSON을 주십시오.";
                }
            }
        }
    }

    /**
     * 도시별 맛집 목록을 DTO로 반환
     */
    public List<AiRestaurantDTO> getRestaurantsByCityDTO(Integer cityId) {
        List<AiRestaurant> list = aiRestaurantRepository.findByCityId(cityId);
        return list.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * AiRestaurant -> AiRestaurantDTO 변환
     */
    private AiRestaurantDTO convertToDto(AiRestaurant entity) {
        AiRestaurantDTO dto = new AiRestaurantDTO();
        dto.setId(entity.getId());
        dto.setCityId(entity.getCityId());
        dto.setName(entity.getName());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setRating(entity.getRating());
        dto.setDescription(entity.getDescription());
        dto.setImageUrl(entity.getImageUrl());
        return dto;
    }

    /**
     * 프롬프트 생성 예시: 도시 이름 + 국가 이름을 사용
     * - 백틱/마크다운을 쓰지 말라고 강조
     */
    private String buildPromptForCity(City city) {
        String cityName = city.getName();
        String countryName = "미상"; // 필요시 country 테이블로부터 가져오기

        // 중간에 “절대 백틱(`)이나 ```를 사용하지 말고” 문구 추가
        // “JSON만 출력” 문구 추가
        return """
        도시는 "%s"이고, 국가는 "%s"입니다.
        이 도시에서 인기 있는 맛집 5곳을 선정해줘.
        각 맛집에 대해 다음 정보를 포함해야 해:
        1) "name": 맛집 이름 (String)
        2) "latitude": 위도 (Number, 소수점 7자리 정도)
        3) "longitude": 경도 (Number, 소수점 7자리 정도)
        4) "rating": 평점 (Number, 5점 만점, 소수점 둘째 자리)
        5) "description": 맛집에 대한 설명 (String, 메뉴 하이라이트 등)
        6) "image_url": 이미지 URL (String)

        오직 JSON 배열 형태로만 반환해줘.
        절대 백틱(`)이나 ```를 사용하지 말고, JSON만 출력해.

        예시:
        [
          {
            "name": "식당 A",
            "latitude": 35.1234567,
            "longitude": 139.1234567,
            "rating": 4.50,
            "description": "주요 메뉴와 간단한 소개",
            "image_url": "https://example.com/restaurantA.jpg"
          },
          ...
        ]

        반드시 5개의 항목을 포함해줘.
        """.formatted(cityName, countryName);
    }

    /**
     * 채팅 응답에서 message.content 추출
     */
    private String extractContentFromChat(OpenAiResponseDTO response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "[]";
        }
        OpenAiResponseDTO.Choice choice = response.getChoices().get(0);
        if (choice.getMessage() == null) {
            return "[]";
        }
        return choice.getMessage().getContent();
    }

    /**
     * JSON 파싱용 내부 DTO (AI 응답에서 1개 맛집 정보)
     */
    @lombok.Data
    static class RestaurantItem {
        private String name;
        private double latitude;
        private double longitude;
        private double rating;
        private String description;
        private String image_url;
    }
}
