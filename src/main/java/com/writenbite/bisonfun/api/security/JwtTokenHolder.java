package com.writenbite.bisonfun.api.security;

import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.function.Function;

public class JwtTokenHolder {
    private final Claims claims;

    public JwtTokenHolder(String token, Key key) throws TokenValidationException, TokenExpiredException {
        Claims tokenClaims;
        try {
            tokenClaims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new TokenValidationException();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        }
        claims = tokenClaims;
    }

    private <T> T extractClaim(Function<Claims, T> claimResolver) {
        return claimResolver.apply(claims);
    }

    public String extractUserName() {
        return extractClaim(Claims::getSubject);
    }

    public TokenType extractTokenType() {
        return extractClaim(tokenClaims -> {
            try {
                return TokenType.valueOf(tokenClaims.get("tot", String.class));
            } catch (IllegalArgumentException e) {
                return TokenType.NO_TOKEN;
            }
        });
    }

    public Boolean validate(UserDetails userDetails, AuthorityMapper authorityMapper) {
        final String userName = extractUserName();
        TokenType tokenType = extractTokenType();
        return userName.equals(userDetails.getUsername()) && userDetails.getAuthorities().contains(authorityMapper.fromTokenType(tokenType));
    }

}
