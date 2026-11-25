package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long accessTokenTtlSeconds;

    public JwtService(@Value("${security.jwt.secret}") String secretKey,
                      @Value("${security.jwt.access-ttl-seconds}") long accessTokenTtlSeconds,
                      @Value("${security.jwt.issuer}") String issuer) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("JWT secret key must not be null or empty");
        }

        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    // Generate access token with user role and type claims
    public String generateJwtToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claims(Map.of(
                        "role", user.getRole(),
                        "type", "access"
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
                .issuer(issuer)
                .signWith(secretKey)
                .compact();

    }

    // Generate refresh token with a unique identifier (jti) for revocation purposes
    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getUsername())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds * 24 * 7))) // Refresh token valid for 7 days
                .issuer(issuer)
                .signWith(secretKey)
                .compact();
    }

    // Parse and validate the JWT token, returning the claims if valid
    public Jws<Claims> parseJwtToken(String authToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(authToken);
    }

    public boolean isAccessToken(String authToken) {
        Claims claims = parseJwtToken(authToken).getPayload();
        return "access".equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(String authToken) {
        Claims claims = parseJwtToken(authToken).getPayload();
        return "refresh".equals(claims.get("type", String.class));
    }

    public String extractUsername(String authToken) {
        return extractClaims(authToken, Claims::getSubject);
    }

    public <T> T extractClaims(String authToken, Function<Claims, T> claimsResolver) {
        Claims claims = extractClaims(authToken);
        return claimsResolver.apply(claims);
    }

    private Claims extractClaims(String authToken) {
        return parseJwtToken(authToken).getPayload();
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