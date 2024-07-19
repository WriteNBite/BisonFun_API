package com.writenbite.bisonfun.api.mutation;

import com.writenbite.bisonfun.api.security.TokenExpiredException;
import com.writenbite.bisonfun.api.security.TokenValidationException;
import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.security.JwtTokenHolder;
import com.writenbite.bisonfun.api.security.TokenType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureHttpGraphQlTester
public class TokenRefreshmentTest {
    @Autowired
    private HttpGraphQlTester tester;
    @Autowired
    private JwtService jwtService;

    @Test
    public void refreshAccessTokenSuccess() throws TokenExpiredException, TokenValidationException {
        String refreshToken = jwtService.generateToken("RUser", 30000, TokenType.REFRESH);
        String accessToken = tester.mutate()
                .header("Authorization", "Bearer " + refreshToken)
                .build()
                .documentName("api/mutation/refreshAccessTokenTest")
                .execute()
                .errors()
                .verify()
                .path("refreshAccessToken.token")
                .hasValue()
                .entity(String.class)
                .get();

        JwtTokenHolder accessTokenHolder = jwtService.getHolder(accessToken);
        assertEquals("RUser", accessTokenHolder.extractUserName());
        assertEquals(TokenType.ACCESS, accessTokenHolder.extractTokenType());
    }

    @Test
    public void refreshTokenSuccess() throws TokenExpiredException, TokenValidationException {
        String generatedToken = jwtService.generateToken("RUser", 30000, TokenType.REFRESH);
        GraphQlTester.Response response = tester.mutate()
                .header("Authorization", "Bearer " + generatedToken)
                .build()
                .documentName("api/mutation/refreshTokenTest")
                .execute();
        response.errors()
                .verify();
        String accessToken = response
                .path("refreshToken.token")
                .hasValue()
                .entity(String.class)
                .get();
        String refreshToken = response
                .path("refreshToken.refresh")
                .hasValue()
                .entity(String.class)
                .get();
        JwtTokenHolder accessTokenHolder = jwtService.getHolder(accessToken);
        JwtTokenHolder refreshTokenHolder = jwtService.getHolder(refreshToken);
        assertEquals("RUser", accessTokenHolder.extractUserName());
        assertEquals("RUser", refreshTokenHolder.extractUserName());
        assertEquals(TokenType.ACCESS, accessTokenHolder.extractTokenType());
        assertEquals(TokenType.REFRESH, refreshTokenHolder.extractTokenType());
    }
}
