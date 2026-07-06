package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.ResetPassword;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderMail;

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public void sendOtpMail(String email, String otp) throws UnsupportedEncodingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(String.valueOf(new InternetAddress(senderMail, "Skill_Drill")));
        message.setSubject("OTP Verification");
        message.setText("Your OTP for Skill_Drill account verification: " + otp +
                "\n\nThis OTP is valid for 10 minutes. Do not share it with anyone.");
        mailSender.send(message);
    }

    public void sendResetPasswordLink(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String frontendLink = "http://localhost:4200/reset-password?token=" + token;

            helper.setFrom(senderMail, "Skill_Drill");
            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                            "<h2>Welcome to Skill_Drill!</h2>" +
                            "<p>Click the button below to reset your password:</p>" +
                            "<a href='" + frontendLink + "' style='" +
                            "display: inline-block; " +
                            "padding: 10px 20px; " +
                            "color: white; " +
                            "background-color: #22c55e; " +
                            "text-decoration: none; " +
                            "border-radius: 5px; " +
                            "font-weight: bold;'>" +
                            "Reset My Password" +
                            "</a>" +
                            "<p style='color: #666; font-size: 12px; margin-top: 20px;'>" +
                            "If the button doesn't work, copy and paste this link into your browser:<br>" +
                            frontendLink +
                            "</p>" +
                            "</div>";

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Account Password");

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}