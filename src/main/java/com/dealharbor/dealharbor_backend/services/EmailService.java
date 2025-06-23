package com.dealharbor.dealharbor_backend.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        String subject = "DealHarbor Email Verification OTP";
        String text = "Your OTP for DealHarbor registration is: " + otp + "\n\nIt expires in 15 minutes.";
        sendMail(to, subject, text);
    }

    public void sendForgotPasswordOtp(String to, String otp) {
        String subject = "DealHarbor Password Reset OTP";
        String text = "Your OTP for resetting your DealHarbor password is: " + otp + "\n\nIt expires in 15 minutes.";
        sendMail(to, subject, text);
    }

    private void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
