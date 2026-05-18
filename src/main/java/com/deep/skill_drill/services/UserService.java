package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.SkillRepo;
import com.deep.skill_drill.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private SkillRepo skillRepo;

    public User registerUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return this.userRepo.save(user);
    }

    public String verifyUser(User user) {
        User freshUser = this.userRepo.findByUsername(user.getUsername());
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            if (authenticate.isAuthenticated()) {
                return jwtService.generateJwtToken(freshUser);
            }
        }
        catch (BadCredentialsException e) {
            return "Invalid username or password";
        }
        return null;
    }

    @Transactional
    public boolean verifyAndEnableUser(String email) {
        User user = userRepo.findByUsername(email);
        if (user == null) {
            return false;
        }

        user.setVerified(true);
        return true;
    }

    @Transactional
    public User updateUserSkills(String email, List<String> skillName) {
        User user = userRepo.findByUsername(email);
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

}
