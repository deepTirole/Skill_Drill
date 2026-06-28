package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.LoginCredential;
import com.deep.skill_drill.dto.RatingPointDto;
import com.deep.skill_drill.entities.PendingRegistration;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.InterviewRepo;
import com.deep.skill_drill.repositories.PendingRepo;
import com.deep.skill_drill.repositories.SkillRepo;
import com.deep.skill_drill.repositories.UserRepo;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PendingRepo pendingRepo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final JwtService jwtService;
    private final SkillRepo skillRepo;
    private final InterviewRepo interviewRepo;

    public UserService(UserRepo userRepo, PendingRepo pendingRepo, BCryptPasswordEncoder encoder,
                       AuthenticationManager authenticationManager, MailService mailService,
                       JwtService jwtService, SkillRepo skillRepo, InterviewRepo interviewRepo) {
        this.userRepo = userRepo;
        this.pendingRepo = pendingRepo;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.skillRepo = skillRepo;
        this.interviewRepo = interviewRepo;
    }

    // ─── Registration ────────────────────────────────────────────────────────────

    @Transactional
    public void registerUser(User user) {
        // 1. Block if already a verified user
        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        String otp = mailService.generateOtp();

        // 2. If pending entry exists — refresh OTP and resend
        PendingRegistration existing = pendingRepo.findByUsername(user.getUsername());
        if (existing != null) {
            existing.setOtpHash(encoder.encode(otp));
            existing.setAttempts(0);
            existing.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            pendingRepo.save(existing);
            sendOrRollback(existing, otp);
            return;
        }

        // 3. New registration — save to pending, NOT users table
        PendingRegistration pending = new PendingRegistration();
        pending.setUsername(user.getUsername());
        pending.setFullname(user.getFullname());
        pending.setPassword(encoder.encode(user.getPassword()));
        pending.setOtpHash(encoder.encode(otp));
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        pendingRepo.save(pending);
        sendOrRollback(pending, otp);
    }

    private void sendOrRollback(PendingRegistration pending, String otp) {
        try {
            mailService.sendOtpMail(pending.getUsername(), otp);
        } catch (Exception e) {
            pendingRepo.delete(pending);
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    // ─── OTP Verification ────────────────────────────────────────────────────────

    @Transactional
    public String verifyAndEnableUser(String username, String otp) {
        PendingRegistration pending = pendingRepo.findByUsername(username);

        if (pending == null) {
            return "SESSION_EXPIRED";
        }

        if (LocalDateTime.now().isAfter(pending.getExpiresAt())) {
            String newOtp = mailService.generateOtp();
            pending.setOtpHash(encoder.encode(newOtp));
            pending.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            pendingRepo.save(pending);
            this.sendOrRollback(pending, newOtp);
            return "OTP_EXPIRED";
        }

        if (pending.getAttempts() >= 5) {
            pendingRepo.delete(pending);
            return "TOO_MANY_ATTEMPTS";
        }

        if (!encoder.matches(otp, pending.getOtpHash())) {
            pending.setAttempts(pending.getAttempts() + 1);
            pendingRepo.save(pending);
            return "INVALID_OTP";
        }

        User newUser = new User();
        newUser.setUsername(pending.getUsername());
        newUser.setFullname(pending.getFullname());
        newUser.setPassword(pending.getPassword());
        userRepo.save(newUser);

        pendingRepo.delete(pending);
        return "SUCCESS";
    }

    // ─── Login ───────────────────────────────────────────────────────────────────

    public String verifyUser(LoginCredential user) {
        User freshUser = userRepo.findByUsername(user.getUsername());
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );
            if (authenticate.isAuthenticated()) {
                return jwtService.generateJwtToken(freshUser);
            }
        } catch (BadCredentialsException e) {
            return "Invalid username or password";
        }
        throw new RuntimeException("Authentication Failed");
    }

    // ─── User Queries ─────────────────────────────────────────────────────────────

    public User getUser(String username) {
        return userRepo.findByUsername(username);
    }

    public User updateUser(User user) {
        return userRepo.save(user);
    }

    @Transactional
    public User updateUserSkills(Long userId, List<String> skillName) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Set<Skill> userSkills = user.getUserSkills();
        for (String name : skillName) {
            String normalizedSkill = name.trim().toLowerCase();

            Skill skill = skillRepo.findBySkillName(normalizedSkill)
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setSkillName(normalizedSkill);
                        return skillRepo.save(newSkill);
                    });
            userSkills.add(skill);
        }

        return userRepo.save(user);
    }

    public @Nullable List<RatingPointDto> getInterviewRatingHistory(String username) {
        User user = userRepo.findByUsername(username);

        List<RatingPointDto> history = interviewRepo.findRatingHistory(user.getId());

        RatingPointDto start = new RatingPointDto();
        start.setDate(user.getCreatedAt());
        start.setRatingAfter(1000);

        List<RatingPointDto> userRatingHistory = new ArrayList<>();
        userRatingHistory.add(start);
        userRatingHistory.addAll(history);

        return userRatingHistory;
    }

    @Transactional
    public void resendOtp(String email) throws UnsupportedEncodingException {
        String sanitizedEmail = email != null ? email.trim().toLowerCase() : "";
        System.out.println(sanitizedEmail);
        PendingRegistration pending =
                pendingRepo.findByUsername(sanitizedEmail);

        if(pending == null)
            throw new RuntimeException("Session expired!!");

        String otp = mailService.generateOtp();

        pending.setOtpHash(
                encoder.encode(otp));
        pending.setExpiresAt(
                LocalDateTime.now().plusMinutes(10));
        pending.setAttempts(0);
        pendingRepo.save(pending);
        mailService.sendOtpMail(sanitizedEmail, otp);
    }
}