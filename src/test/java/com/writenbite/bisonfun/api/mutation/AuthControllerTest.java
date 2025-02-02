package com.writenbite.bisonfun.api.mutation;

import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.MapperConfig;
import com.writenbite.bisonfun.api.controller.AuthController;
import com.writenbite.bisonfun.api.security.TokenExpiredException;
import com.writenbite.bisonfun.api.security.TokenValidationException;
import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.security.UserPrincipal;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.types.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@GraphQlTest(AuthController.class)
@Import({GraphQlConfig.class, MapperConfig.class})
public class AuthControllerTest {
    @Autowired
    private GraphQlTester tester;
    @Autowired
    private UserMapper userMapper;
    @SpyBean
    private JwtService jwtService;
    @MockBean
    private UserService userService;
    @MockBean
    private AuthenticationManager authenticationManager;
    private User user;


    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("Tester");
        user.setEmail("testing@test.com");
        user.setPassword("pass");
    }

    @Test
    public void loginSuccess() throws TokenExpiredException, TokenValidationException {
        Authentication auth = Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(new UserPrincipal(user));
        GraphQlTester.Response response = tester.documentName("api/mutation/loginTest")
                .execute();

        String accessToken = response.path("login.token")
                .hasValue()
                .entity(String.class)
                .get();
        String refreshToken = response.path("login.refresh")
                .hasValue()
                .entity(String.class)
                .get();
        assertEquals(TokenType.ACCESS, jwtService.getHolder(accessToken).extractTokenType());
        assertEquals(TokenType.REFRESH, jwtService.getHolder(refreshToken).extractTokenType());
    }

    @Test
    public void badCredentialsLogin() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));
        GraphQlTester.Response response = tester.documentName("api/mutation/loginTest")
                .execute();

        response.errors()
                .satisfy(errors -> {
                    assertFalse(errors.isEmpty());
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("BAD_REQUEST");
                });
    }

    @Test
    public void registerSuccess() {
        when(userService.existUserByUsernameOrEmail(anyString(), anyString())).thenReturn(false);
        when(userService.saveUser(any())).thenReturn(user);
        GraphQlTester.Response response = tester.documentName("api/mutation/registerTest")
                .execute();
        com.writenbite.bisonfun.api.types.user.User actualUser = response.errors()
                .verify()
                .path("register")
                .entity(com.writenbite.bisonfun.api.types.user.User.class)
                .get();
        assertEquals(userMapper.fromEntity(user), actualUser);
    }

    @Test
    public void registerFailed() {
        when(userService.existUserByUsernameOrEmail(anyString(), anyString())).thenReturn(true);

        tester.documentName("api/mutation/registerTest")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.isEmpty()).isFalse();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("BAD_REQUEST");
                });
    }

    @Test
    public void refreshAccessTokenNotAuthorized() {
        tester.documentName("api/mutation/refreshAccessTokenTest")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.isEmpty()).isFalse();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("UNAUTHORIZED");
                });
    }

    @Test
    public void refreshTokenTestNotAuthorized() {
        tester.documentName("api/mutation/refreshTokenTest")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.isEmpty()).isFalse();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("UNAUTHORIZED");
                });
    }
}
