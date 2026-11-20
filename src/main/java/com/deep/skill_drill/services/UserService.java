package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.*;
import com.deep.skill_drill.exceptions.BadRequestException;
import com.deep.skill_drill.exceptions.ConflictException;
import com.deep.skill_drill.exceptions.ExternalServiceException;
import com.deep.skill_drill.exceptions.ResourceNotFoundException;
import com.deep.skill_drill.exceptions.UnauthorizedException;
import com.deep.skill_drill.entities.PendingEmail;
import com.deep.skill_drill.entities.PendingRegistration;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import kotlin.jvm.Throws;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PendingRepo pendingRepo;
    private final BCryptPasswordEncoder encoder;
    @Lazy private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final JwtService jwtService;
    private final SkillRepo skillRepo;
    private final InterviewRepo interviewRepo;
    private final PendingEmailRepo emailRepo;

    @Value("${google.client.id}")
    private String googleClientId;

    public UserService(UserRepo userRepo, PendingRepo pendingRepo, BCryptPasswordEncoder encoder,
                       AuthenticationManager authenticationManager, MailService mailService,
                       JwtService jwtService, SkillRepo skillRepo, InterviewRepo interviewRepo,
                       PendingEmailRepo emailRepo) {
        this.userRepo = userRepo;
        this.pendingRepo = pendingRepo;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.skillRepo = skillRepo;
        this.interviewRepo = interviewRepo;
        this.emailRepo = emailRepo;
    }

    // ─── Registration ────────────────────────────────────────────────────────────

    @Transactional
    public void registerUser(User user) {
        // 1. Block if already a verified user
        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new ConflictException("USERNAME_EXISTS", "Username already exists");
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
            throw new ExternalServiceException("OTP_DELIVERY_FAILED", "Failed to send OTP email. Please try again.", e);
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
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Invalid username or password");
        }
        throw new UnauthorizedException("AUTHENTICATION_FAILED", "Authentication failed");
    }

    @Transactional
    public String verifyAndProcessGoogleUser(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BadRequestException("GOOGLE_TOKEN_INVALID", "Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepo.findByUsername(email);
            if (user == null) {
                user = new User();
                user.setUsername(email);
                user.setFullname(name != null ? name : email.split("@")[0]);
                user.setPassword(encoder.encode(UUID.randomUUID().toString()));
                user.setRole("ROLE_STUDENT");
                user.setRating(1000);
                user = userRepo.save(user);
            }

            return jwtService.generateJwtToken(user);

        } catch (Exception e) {
            throw new UnauthorizedException("GOOGLE_AUTH_FAILED", "Google authentication failed", e);
        }
    }

    // ─── User Queries ─────────────────────────────────────────────────────────────

    public User getUser(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "User not found: " + username);
        }
        return user;
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
            throw new ResourceNotFoundException("OTP_SESSION_NOT_FOUND", "OTP session expired or does not exist");

        String otp = mailService.generateOtp();

        pending.setOtpHash(
                encoder.encode(otp));
        pending.setExpiresAt(
                LocalDateTime.now().plusMinutes(10));
        pending.setAttempts(0);
        pendingRepo.save(pending);
        mailService.sendOtpMail(sanitizedEmail, otp);
    }

    public void generateResetToken(String email) {
        User user = userRepo.findByUsername(email);

        if (user == null) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "User not found");
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetExpiry(LocalDateTime.now().plusMinutes(10));
        mailService.sendResetPasswordLink(email, token);
        userRepo.save(user);
    }

    public void resetPassword(ResetPassword dto) {
        User user = userRepo.findByResetToken(dto.getToken())
                .orElse(null);

        if(user == null || user.getResetExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("RESET_TOKEN_INVALID", "Reset token is invalid or expired");
        }

        dto.setPassword(encoder.encode(dto.getPassword()));
        if(encoder.matches(user.getPassword(), dto.getPassword())) {
            throw new BadRequestException("PASSWORD_REUSED", "New password must differ from the previous password");
        }

        user.setPassword(dto.getPassword());
        user.setResetExpiry(null);
        user.setResetToken(null);
        userRepo.save(user);
    }

    @Transactional
    public void updateUser(UpdateDto user, String username) throws UnsupportedEncodingException {
        User dbUser = userRepo.findByUsername(username);
        if(dbUser == null) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "User not found");
        }

        if(user.getFullname() != null) {
            dbUser.setFullname(user.getFullname());
        }
        else if(user.getUsername() != null) {
            dbUser.setUsername(user.getUsername());
        }
        else if(user.getNewPassword() != null &&  user.getCurrentPassword() != null) {
            if(encoder.matches(user.getCurrentPassword(), dbUser.getPassword())) {
                dbUser.setPassword(encoder.encode(user.getNewPassword()));
            }
            else             throw new UnauthorizedException("PASSWORD_MISMATCH", "Current password is incorrect");
        }

        userRepo.save(dbUser);
        if(user.getUsername() != null) {
            String otp = mailService.generateOtp();
            mailService.sendOtpMail(user.getUsername(), otp);
        }
    }

    @Transactional
    public void initiateEmailUpdate(EmailUpdate dto, String username) {
        User user = userRepo.findByUsername(username);
        if(user == null)
            throw new ResourceNotFoundException("USER_NOT_FOUND", "User not found");

        if(!encoder.matches(dto.getPassword(), user.getPassword()))
            throw new UnauthorizedException("INVALID_PASSWORD", "Current password is incorrect");

        if(username.equals(dto.getEmail()))
            throw new BadRequestException("EMAIL_UNCHANGED", "New email must differ from the current email");

        if(userRepo.existsByUsername(dto.getEmail()))
            throw new ConflictException("EMAIL_EXISTS", "An account with this email already exists");

        PendingEmail pendingEmail = emailRepo.findByEmail(dto.getEmail());
        if(pendingEmail == null)
            pendingEmail = new PendingEmail();

        String otp = mailService.generateOtp();
        try {
            mailService.sendEmailUpdateOtp(dto.getEmail(), otp);

            pendingEmail.setEmail(dto.getEmail());
            pendingEmail.setOtpHash(encoder.encode(otp));
            pendingEmail.setExpiresAt(LocalDateTime.now().plusMinutes(15));

            emailRepo.save(pendingEmail);
        } catch (RuntimeException e) {
            throw new ExternalServiceException("EMAIL_DELIVERY_FAILED", "Email could not be sent. Please try again later", e);
        }
    }

    @Transactional
    public User finalizeEmailUpdate(OtpPayload dto, String username) {
        User user  = userRepo.findByUsername(username);
        if(user == null)
            throw new ResourceNotFoundException("USER_NOT_FOUND", "User not found");

        PendingEmail pendingEmail = emailRepo.findByEmail(dto.getEmail());
        if(pendingEmail == null)
            throw new BadRequestException("EMAIL_UPDATE_SESSION_INVALID", "Email update session is invalid. Please start again");

        if(!encoder.matches(dto.getOtp(), pendingEmail.getOtpHash()) ||
            LocalDateTime.now().isAfter(pendingEmail.getExpiresAt())
        )
            throw new BadRequestException("OTP_INVALID_OR_EXPIRED", "OTP is invalid or expired. Please resend it") ;

        user.setUsername(dto.getEmail());
        emailRepo.delete(pendingEmail);

        return userRepo.save(user);
    }
}