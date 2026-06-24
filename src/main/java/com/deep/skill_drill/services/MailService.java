package com.deep.skill_drill.services;

import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderMail;

    // Store OTP per email
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> otpTimestamps = new ConcurrentHashMap<>();

    @Async
    public void sendMail(String email) throws UnsupportedEncodingException {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        System.out.println(otp);

        otpStore.put(email, otp);
        otpTimestamps.put(email, Instant.now());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(String.valueOf(new InternetAddress(senderMail, "Skill_Drill")));
        message.setSubject("OTP Verification");
        message.setText("Your OTP for Skill_Drill account verification: " + otp);

        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {
        String stored = otpStore.get(email);
        Instant timestamp = otpTimestamps.get(email);

        if (stored != null && stored.equals(otp) && timestamp != null) {
            boolean valid = Instant.now().isBefore(timestamp.plusSeconds(300));
            if (valid) {
                // Clean up after successful verification
                otpStore.remove(email);
                otpTimestamps.remove(email);
            }
            return valid;
        }
        return false;
    }
}