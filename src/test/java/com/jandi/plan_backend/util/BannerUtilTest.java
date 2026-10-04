package com.jandi.plan_backend.util;

import com.jandi.plan_backend.image.repository.ImageRepository;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.resource.banner.entity.Banner;
import com.jandi.plan_backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BannerUtilTest {

    @Mock
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private BannerUtil bannerUtil;

    @Test
    void 거부될_파일이면_배너_이미지를_교체하기_전에_기존_이미지를_지우지_않는다() {
        MockMultipartFile html = new MockMultipartFile("file", "x.html", "text/html", new byte[]{1});
        doThrow(new IllegalArgumentException("허용되지 않는 파일 형식입니다.")).when(imageService).validateUpload(html);

        assertThatThrownBy(() -> bannerUtil.replaceBannerImage(new Banner(), new User(), html))
                .isInstanceOf(IllegalArgumentException.class);

        verify(imageRepository, never()).findByTargetTypeAndTargetId(any(), any());
        verify(imageService, never()).deleteImage(any());
    }
}
