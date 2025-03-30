package com.jandi.plan_backend.tripPlan.trip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.tripPlan.trip.dto.TripRespDTO;
import com.jandi.plan_backend.tripPlan.trip.entity.Trip;
import com.jandi.plan_backend.tripPlan.trip.repository.TripLikeRepository;
import com.jandi.plan_backend.tripPlan.trip.repository.TripParticipantRepository;
import com.jandi.plan_backend.tripPlan.trip.repository.TripRepository;
import com.jandi.plan_backend.tripPlan.itinerary.repository.ItineraryRepository;
import com.jandi.plan_backend.tripPlan.reservation.repository.ReservationRepository;
import com.jandi.plan_backend.user.entity.City;
import com.jandi.plan_backend.user.entity.Country;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.CityRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TripServiceTest {

    @InjectMocks
    private TripService tripService;

    @Mock
    private TripRepository tripRepository;
    @Mock
    private TripLikeRepository tripLikeRepository;
    @Mock
    private ValidationUtil validationUtil;
    @Mock
    private ImageService imageService;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private ItineraryRepository itineraryRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TripParticipantRepository tripParticipantRepository;

    @Test
    public void testWriteTrip_success() {
        // Given
        String userEmail = "test@example.com";
        String title = "Test Trip";
        String startDate = "2023-04-01";
        String endDate = "2023-04-10";
        String privatePlan = "no";  // 공개 계획
        Integer budget = 1000;
        Integer cityId = 1;

        // 더미 User 생성
        User dummyUser = new User();
        dummyUser.setUserId(1);
        dummyUser.setEmail(userEmail);
        dummyUser.setUserName("Test User");

        // 더미 Country 생성
        Country dummyCountry = new Country();
        dummyCountry.setName("TestCountry");

        // 더미 City 생성 (Country 정보 포함)
        City dummyCity = new City();
        dummyCity.setCityId(cityId);
        dummyCity.setName("TestCity");
        dummyCity.setLatitude(10.0);
        dummyCity.setLongitude(20.0);
        dummyCity.setCountry(dummyCountry);

        // validationUtil 모킹 설정
        when(validationUtil.validateUserExists(userEmail)).thenReturn(dummyUser);
        when(validationUtil.ValidateDate(startDate)).thenReturn(LocalDate.of(2023, 4, 1));
        when(validationUtil.ValidateDate(endDate)).thenReturn(LocalDate.of(2023, 4, 10));
        when(validationUtil.validateCityExists(cityId)).thenReturn(dummyCity);

        // tripRepository.save() 호출 시, tripId를 설정하도록 모킹
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setTripId(1); // 저장 시 id 설정
            return trip;
        });

        // imageService 모킹 (이미지가 없는 경우)
        when(imageService.getImageByTarget(anyString(), anyInt())).thenReturn(Optional.empty());
        when(imageService.getPublicUrlByImageId(1)).thenReturn("default_url");

        // When: writeTrip 메서드 호출
        TripRespDTO responseDTO = tripService.writeTrip(userEmail, title, startDate, endDate, privatePlan, budget, cityId);

        // Then: 반환된 DTO 검증
        assertNotNull(responseDTO);
        assertEquals(1, responseDTO.getTripId());
        assertEquals(title, responseDTO.getTitle());
    }
}
