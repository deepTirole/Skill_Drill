package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret_key;

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secret_key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("isVerified", user.isVerified());

        return Jwts.builder().claims().add(claims).and()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String authToken) {
        return extractClaims(authToken, Claims::getSubject);
    }

    public <T> T extractClaims(String authToken, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(authToken);
        return claimsResolver.apply(claims);
    }

    private Claims extractClaims(String authToken) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(authToken)
                .getPayload();
    }

    public boolean isTokenValid(String authToken, UserDetails userDetails) {
        String username = extractUsername(authToken);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(authToken));
    }

    private boolean isTokenExpired(String authToken) {
        return expirationTime(authToken).before(new Date());
    }

    private Date expirationTime(String authToken) {
        return extractClaims(authToken, Claims::getExpiration);
    }

    public String extractRole(String authToken) {
        return extractClaims(authToken, claim -> claim.get("role", String.class));
    }

    public Boolean extractVerified(String authToken) {
        return extractClaims(authToken, claim -> claim.get("isVerified", Boolean.class));
    }
}
