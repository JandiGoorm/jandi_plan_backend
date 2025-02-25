package com.jandi.plan_backend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // 이메일 전송을 위한 JavaMailSender 빈을 주입받음.
    private final JavaMailSender mailSender;

    /**
     * 단순 텍스트 이메일을 전송하는 메서드.
     *
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param text 이메일 본문
     */
    public void sendSimpleMail(String to, String subject, String text) {
        // SimpleMailMessage 객체를 생성해서 이메일 메시지 내용을 설정.
        SimpleMailMessage message = new SimpleMailMessage();

        // 수신자 이메일 주소를 설정.
        message.setTo(to);

        // 이메일 제목을 설정.
        message.setSubject(subject);

        // 이메일 본문 텍스트를 설정.
        message.setText(text);

        // 설정된 메시지를 mailSender를 통해 전송.
        mailSender.send(message);
    }
}
