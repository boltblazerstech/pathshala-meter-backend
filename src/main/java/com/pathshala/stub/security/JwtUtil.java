package com.pathshala.stub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyWithAtLeast32CharactersForHmacSha256}")
    private String secretString;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a signed JWT.
     * @param subject   the JWT `sub` claim (user_id or admin_id as UUID string)
     * @param extraClaims  additional claims to embed (role, user_id, admin_id, etc.)
     */
    public String generateToken(String subject, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)   // set extra claims first
                .subject(subject)      // sub overrides any "sub" in extraClaims
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Extract the `sub` claim (user_id or admin_id). */
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Extract any named claim as a String. Returns null if absent. */
    public String extractClaim(String token, String claimName) {
        Object value = extractAllClaims(token).get(claimName);
        return value != null ? value.toString() : null;
    }

    /** Return true if the token signature is valid and not expired. */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
