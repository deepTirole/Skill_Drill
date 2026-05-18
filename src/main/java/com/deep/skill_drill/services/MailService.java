package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserService userService;

    @Value("${spring.mail.username}")
    private String senderMail;
    private String lastOtp;
    private Instant otpTimeStamp;

    @Async
    public void sendMail(String email) {
        String otp = String.format("%04d", new Random().nextInt(10000));

        lastOtp = otp;
        otpTimeStamp = Instant.now();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setFrom(senderMail);
        message.setSubject("OTP Verification");
        message.setText("Your OTP for skill_drill account verification " + otp);

        mailSender.send(message);
    }

    public boolean verifyOtp(String otp) {
        if(lastOtp != null && lastOtp.equals(otp)) {
            return Instant.now().isBefore(otpTimeStamp.plusSeconds(300));
        }
        return false;
    }
}
