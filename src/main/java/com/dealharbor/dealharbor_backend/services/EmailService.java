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

    public void sendEmailChangeOtp(String to, String otp) {
        String subject = "DealHarbor Email Change Verification";
        String text = "Your OTP for changing your DealHarbor email is: " + otp + "\n\nIt expires in 15 minutes.";
        sendMail(to, subject, text);
    }

    public void sendStudentVerificationOtp(String to, String otp, String userName) {
        String subject = "DealHarbor Student Verification OTP";
        String text = "Hi " + userName + ",\n\n" +
                     "Your OTP for verifying your VIT student email on DealHarbor is: " + otp + "\n\n" +
                     "This OTP will expire in 15 minutes.\n\n" +
                     "Once verified, you'll get access to student-only features and your listings will show a verified student badge.\n\n" +
                     "Best regards,\nDealHarbor Team";
        sendMail(to, subject, text);
    }

    public void sendSecurityAlert(String to, String alertType, String message) {
        String subject = "DealHarbor Security Alert: " + alertType;
        String text = "Security Alert for your DealHarbor account:\n\n" + message + 
                     "\n\nIf this wasn't you, please contact support immediately.";
        sendMail(to, subject, text);
    }

    public void sendLoginNotification(String to, String ipAddress, String deviceInfo) {
        String subject = "New Login to Your DealHarbor Account";
        String text = "A new login was detected on your DealHarbor account:\n\n" +
                     "IP Address: " + ipAddress + "\n" +
                     "Device: " + deviceInfo + "\n" +
                     "Time: " + java.time.Instant.now() + "\n\n" +
                     "If this wasn't you, please secure your account immediately.";
        sendMail(to, subject, text);
    }

    public void sendAccountDeletionConfirmation(String to, String name) {
        String subject = "DealHarbor Account Deletion Confirmation";
        String text = "Hi " + name + ",\n\n" +
                     "Your DealHarbor account has been successfully deleted.\n\n" +
                     "If you didn't request this deletion, please contact support immediately.\n\n" +
                     "Thank you for using DealHarbor!\n\n" +
                     "Best regards,\nDealHarbor Team";
        sendMail(to, subject, text);
    }

    public void sendStudentVerificationSuccess(String to, String name) {
        String subject = "Welcome to DealHarbor - Student Verification Complete!";
        String text = "Hi " + name + ",\n\n" +
                     "Congratulations! Your VIT student email has been successfully verified.\n\n" +
                     "You now have access to:\n" +
                     "• Verified student badge on your profile\n" +
                     "• Student-only marketplace features\n" +
                     "• Enhanced trust and credibility\n\n" +
                     "Start exploring the marketplace and connect with fellow VIT students!\n\n" +
                     "Best regards,\nDealHarbor Team";
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
