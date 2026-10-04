package com.jandi.plan_backend.image.controller;

import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.security.CustomUserDetails;
import com.jandi.plan_backend.tripPlan.trip.service.TripService;
import com.jandi.plan_backend.user.entity.Role;
import com.jandi.plan_backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanImageControllerTest {

    @Mock
    private ImageService imageService;

    @Mock
    private TripService tripService;

    private CustomUserDetails details() {
        User user = mock(User.class);
        when(user.getRoleEnum()).thenReturn(Role.USER);
        lenient().when(user.getUserId()).thenReturn(7);
        lenient().when(user.getEmail()).thenReturn("a@example.com");
        return new CustomUserDetails(user);
    }

    private void givenExistingImage() {
        Image existing = new Image();
        existing.setImageId(99);
        lenient().when(imageService.getImageByTarget(any(), any())).thenReturn(Optional.of(existing));
        lenient().when(tripService.isOwnerOfTrip(any(), any(Integer.class))).thenReturn(true);
    }

    @Test
    void 거부될_파일이면_교체_업로드_전에_기존_이미지를_지우지_않는다() {
        PlanImageController controller = new PlanImageController(imageService, tripService);
        CustomUserDetails details = details();
        givenExistingImage();
        MockMultipartFile html = new MockMultipartFile("file", "x.html", "text/html", new byte[]{1});
        doThrow(new IllegalArgumentException("허용되지 않는 파일 형식입니다.")).when(imageService).validateUpload(html);

        assertThatThrownBy(() -> controller.uploadProfileImage(html, details))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> controller.uploadTripImage(html, 5, details))
                .isInstanceOf(IllegalArgumentException.class);

        verify(imageService, never()).deleteImage(any());
    }

    @Test
    void 저장에_실패하면_교체_업로드해도_기존_이미지를_지우지_않는다() {
        PlanImageController controller = new PlanImageController(imageService, tripService);
        CustomUserDetails details = details();
        givenExistingImage();
        MockMultipartFile png = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(imageService.uploadImage(any(), any(), any(), any())).thenThrow(new IllegalStateException("disk full"));

        assertThatThrownBy(() -> controller.uploadProfileImage(png, details))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> controller.uploadTripImage(png, 5, details))
                .isInstanceOf(IllegalStateException.class);

        verify(imageService, never()).deleteImage(any());
    }
}
