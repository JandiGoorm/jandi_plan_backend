package com.jandi.plan_backend.trip.service;

import com.jandi.plan_backend.image.dto.ImageResponseDto;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.trip.dto.TripRespDTO;
import com.jandi.plan_backend.trip.entity.Trip;
import com.jandi.plan_backend.trip.repository.TripRepository;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.PaginationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final ValidationUtil validationUtil;
    private final ImageService imageService;

    public TripService(TripRepository tripRepository, ValidationUtil validationUtil, ImageService imageService) {
        this.tripRepository = tripRepository;
        this.validationUtil = validationUtil;
        this.imageService = imageService;
    }

    /** 여행 계획 생성 */
    public TripRespDTO writeTrip(
            String userEmail, String title, String description, String startDate, String endDate, String isPrivate, MultipartFile image){
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);

        //새 여행계획 생성
        Trip trip = new Trip();
        trip.setTitle(title);
        trip.setDescription(description);
        trip.setStartDate(LocalDate.parse(startDate));
        trip.setEndDate(LocalDate.parse(endDate));
        trip.setUser(user);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        trip.setLikeCount(0);
        trip.setPrivatePlan(Objects.equals(isPrivate, "yes"));
        tripRepository.save(trip);

        //이미지 저장
        ImageResponseDto imageDTO = imageService.uploadImage(
                image, userEmail, trip.getTripId(), "trip");
        trip.setImageUrl(imageDTO.getImageUrl());
        tripRepository.save(trip);

        return new TripRespDTO(trip, imageService);
    }
}
