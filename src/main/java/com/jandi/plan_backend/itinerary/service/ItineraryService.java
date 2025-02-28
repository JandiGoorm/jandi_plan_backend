package com.jandi.plan_backend.itinerary.service;

import com.jandi.plan_backend.itinerary.dto.ItineraryReqDTO;
import com.jandi.plan_backend.itinerary.dto.ItineraryRespDTO;
import com.jandi.plan_backend.itinerary.entity.Itinerary;
import com.jandi.plan_backend.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItineraryService {
    private final ItineraryRepository itineraryRepository;
    private final ValidationUtil validationUtil;

    public ItineraryService(ItineraryRepository itineraryRepository, ValidationUtil validationUtil) {
        this.itineraryRepository = itineraryRepository;
        this.validationUtil = validationUtil;
    }

    /**
     * 특정 여행 계획에 속한 모든 일정 조회
     * @param userEmail : 토큰이 없는 경우 null, 있는 경우 요청 사용자 이메일
     * @param tripId    : 조회할 여행 계획의 ID
     * @return 일정 목록 DTO 리스트
     */
    public List<ItineraryRespDTO> getItineraries(String userEmail, Integer tripId) {
        // 여행 계획 검증
        Trip trip = validationUtil.validateTripExists(tripId);
        // 만약 토큰이 없는 경우 (userEmail == null)
        // 여행 계획이 private이면 접근 거부
        if (userEmail == null && trip.getPrivatePlan()) {
            throw new BadRequestExceptionMessage("비공개 여행 계획은 접근할 수 없습니다. 액세스토큰이 필요합니다.");
        }
        // 토큰이 있을 경우, private일 때 작성자 여부 확인
        if (userEmail != null && trip.getPrivatePlan()) {
            User user = validationUtil.validateUserExists(userEmail);
            if (!trip.getUser().getUserId().equals(user.getUserId())) {
                throw new BadRequestExceptionMessage("비공개 여행 계획은 작성자만 조회할 수 있습니다.");
            }
        }
        // 조건을 통과하면 해당 여행 계획의 모든 일정 조회
        List<Itinerary> itineraries = itineraryRepository.findByTrip_TripId(tripId);
        return itineraries.stream()
                .map(ItineraryRespDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 일정 생성
     */
    public ItineraryRespDTO createItinerary(String userEmail, Integer tripId, ItineraryReqDTO reqDTO) {
        // 사용자 및 여행 계획 검증
        User user = validationUtil.validateUserExists(userEmail);
        Trip trip = validationUtil.validateTripExists(tripId);
        // 여행 계획의 작성자와 요청 사용자가 일치하는지 확인
        if (!trip.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획에 대해서만 일정을 추가할 수 있습니다.");
        }
        // DTO의 문자열 데이터를 LocalDate, LocalTime으로 변환
        LocalDate date = LocalDate.parse(reqDTO.getDate());
        LocalTime startTime = LocalTime.parse(reqDTO.getStartTime());
        LocalTime endTime = LocalTime.parse(reqDTO.getEndTime());

        // 새로운 Itinerary 엔티티 생성
        Itinerary itinerary = new Itinerary();
        itinerary.setTrip(trip);
        itinerary.setPlaceId(reqDTO.getPlaceId());
        itinerary.setDate(date);
        itinerary.setStartTime(startTime);
        itinerary.setTitle(reqDTO.getTitle());
        itinerary.setCost(reqDTO.getCost());
        // createdAt은 여기서는 date로 설정 (실제 상황에 맞게 조정)
        itinerary.setCreatedAt(date);

        itineraryRepository.save(itinerary);
        return new ItineraryRespDTO(itinerary);
    }

    /**
     * 일정 수정
     */
    public ItineraryRespDTO updateItinerary(String userEmail, Long itineraryId, ItineraryReqDTO reqDTO) {
        // 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        // 일정 검증
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 일정입니다."));
        // 일정이 속한 여행 계획의 작성자와 요청 사용자가 일치하는지 확인
        if (!itinerary.getTrip().getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획의 일정만 수정할 수 있습니다.");
        }
        // 수정 가능한 필드 업데이트
        if (reqDTO.getPlaceId() != null) {
            itinerary.setPlaceId(reqDTO.getPlaceId());
        }
        if (reqDTO.getDate() != null) {
            itinerary.setDate(LocalDate.parse(reqDTO.getDate()));
        }
        if (reqDTO.getStartTime() != null) {
            itinerary.setStartTime(LocalTime.parse(reqDTO.getStartTime()));
        }
        if (reqDTO.getTitle() != null) {
            itinerary.setTitle(reqDTO.getTitle());
        }
        if (reqDTO.getCost() != null) {
            itinerary.setCost(reqDTO.getCost());
        }
        itineraryRepository.save(itinerary);
        return new ItineraryRespDTO(itinerary);
    }

    /**
     * 일정 삭제
     */
    public boolean deleteItinerary(String userEmail, Long itineraryId) {
        // 사용자 검증
        User user = validationUtil.validateUserExists(userEmail);
        // 일정 검증
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new BadRequestExceptionMessage("존재하지 않는 일정입니다."));
        // 일정이 속한 여행 계획의 작성자와 요청 사용자가 일치하는지 확인
        if (!itinerary.getTrip().getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestExceptionMessage("본인이 작성한 여행 계획의 일정만 삭제할 수 있습니다.");
        }
        itineraryRepository.delete(itinerary);
        return true;
    }
}
