package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.security.JwtTokenHolder;
import com.writenbite.bisonfun.api.security.TokenExpiredException;
import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.security.TokenValidationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {
    @Value("${bisonfun.jwt.secret}")
    private String secret;

    public String generateToken(String username, long expirationInMillis, TokenType tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tot", tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenHolder getHolder(String token) throws TokenExpiredException, TokenValidationException {
        return new JwtTokenHolder(token, getSignKey());
    }
}
