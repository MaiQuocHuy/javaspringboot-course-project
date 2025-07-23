package project.ktc.springboot_app.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.security.exception.ExpiredJwtTokenException;
import project.ktc.springboot_app.security.exception.InvalidJwtTokenException;
import project.ktc.springboot_app.security.exception.MalformedJwtTokenException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.security.exception.ExpiredJwtTokenException;
import project.ktc.springboot_app.security.exception.InvalidJwtTokenException;
import project.ktc.springboot_app.security.exception.MalformedJwtTokenException;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.jwt-secret}")
    private String jwtSecret;

    @Value("${jwt.jwt-expiration-ms}")
    private Long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        // Ensure the key is at least 256 bits (32 bytes)
        if (jwtSecret.length() < 32) {
            // If the provided secret is too short, use a secure key generation
            return Jwts.SIG.HS256.key().build();
        }
        // Decode base64 encoded secret if needed
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate Access Token
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Validate token
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new ExpiredJwtTokenException("JWT token is expired", e);
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw new MalformedJwtTokenException("JWT token is malformed", e);
        } catch (SignatureException e) {
            log.error("JWT signature does not match: {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT signature does not match", e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT claims string is empty", e);
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            throw new InvalidJwtTokenException("JWT token validation failed", e);
        }
    }

    // Extract username from token
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            log.error("Cannot extract username from expired JWT token: {}", e.getMessage());
            throw new ExpiredJwtTokenException("Cannot extract username from expired JWT token", e);
        } catch (MalformedJwtException e) {
            log.error("Cannot extract username from malformed JWT token: {}", e.getMessage());
            throw new MalformedJwtTokenException("Cannot extract username from malformed JWT token", e);
        } catch (SignatureException e) {
            log.error("Cannot extract username from JWT token with invalid signature: {}", e.getMessage());
            throw new InvalidJwtTokenException("Cannot extract username from JWT token with invalid signature", e);
        } catch (Exception e) {
            log.error("Cannot extract username from JWT token: {}", e.getMessage());
            throw new InvalidJwtTokenException("Cannot extract username from JWT token", e);
        }
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            log.error("Cannot extract expiration from JWT token: {}", e.getMessage());
            throw new InvalidJwtTokenException("Cannot extract expiration from JWT token", e);
        }
    }

    // Generic claim extractor
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            log.error("Cannot extract claim from expired JWT token: {}", e.getMessage());
            throw new ExpiredJwtTokenException("Cannot extract claim from expired JWT token", e);
        } catch (MalformedJwtException e) {
            log.error("Cannot extract claim from malformed JWT token: {}", e.getMessage());
            throw new MalformedJwtTokenException("Cannot extract claim from malformed JWT token", e);
        } catch (SignatureException e) {
            log.error("Cannot extract claim from JWT token with invalid signature: {}", e.getMessage());
            throw new InvalidJwtTokenException("Cannot extract claim from JWT token with invalid signature", e);
        } catch (Exception e) {
            log.error("Cannot extract claim from JWT token: {}", e.getMessage());
            throw new InvalidJwtTokenException("Cannot extract claim from JWT token", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e; // Re-throw to be caught by caller
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e; // Re-throw to be caught by caller
        } catch (SignatureException e) {
            log.error("JWT signature does not match: {}", e.getMessage());
            throw e; // Re-throw to be caught by caller
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e; // Re-throw to be caught by caller
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e; // Re-throw to be caught by caller
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtTokenException e) {
            return true; // Token is expired
        } catch (Exception e) {
            log.error("Error checking if token is expired: {}", e.getMessage());
            throw new InvalidJwtTokenException("Error checking if token is expired", e);
        }
    }
}
