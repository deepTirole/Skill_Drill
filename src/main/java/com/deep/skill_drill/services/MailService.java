package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.ResetPassword;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Random;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String senderMail;

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public void sendOtpMail(String email, String otp) throws UnsupportedEncodingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(String.valueOf(new InternetAddress(senderMail, "Skill Drill")));
        message.setSubject("OTP Verification");
        message.setText("Your OTP for Skill_Drill account verification: " + otp +
                "\n\nThis OTP is valid for 10 minutes. Do not share it with anyone.");
        mailSender.send(message);
    }

    public void sendEmailUpdateOtp(String targetEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(targetEmail);
        try {
            message.setFrom(String.valueOf(new InternetAddress(senderMail, "Skill Drill")));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to configure sender address for OTP email.", e);
        }
        message.setSubject("Skill Drill - Verify Your New Email Address");

        String emailBody = "Hello,\n\n"
                + "We received a request to update the email address associated with your Skill_Drill account to this one.\n\n"
                + "Please use the following One-Time Password (OTP) to complete the verification process:\n\n"
                + "OTP: " + otp + "\n\n"
                + "This code will expire in 15 minutes.\n"
                + "If you did not request this change, you can safely ignore this email. Your existing account credentials have not been changed.\n\n"
                + "Best regards,\n"
                + "The Skill_Drill Engineering Team";

        message.setText(emailBody);
        mailSender.send(message);
    }

    public void sendResetPasswordLink(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String frontendLink = "http://localhost:4200/reset-password?token=" + token;

            helper.setFrom(senderMail, "Skill Drill");
            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; padding: 20px;'>" +
                            "<h2>Welcome to Skill_Drill!</h2>" +
                            "<p>Click the button below to reset your password:</p>" +
                            "<a href='" + frontendLink + "' style='" +
                            "display: inline-block; padding: 10px 20px; color: white; " +
                            "background-color: #22c55e; text-decoration: none; border-radius: 5px; font-weight: bold;'>" +
                            "Reset My Password</a>" +
                            "<p style='color: #666; font-size: 12px; margin-top: 20px;'>" +
                            "If the button doesn't work, copy and paste this link into your browser:<br>" +
                            frontendLink + "</p></div>";

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Account Password");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to construct or send the password reset email.", e);
        }
    }
}