package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureHttpGraphQlTester
public class AuthUserTest {

    private User user;
    @Autowired
    private HttpGraphQlTester tester;
    @Autowired
    private JwtService jwtService;
    @MockBean
    private UserService userService;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(33);
        user.setUsername("Tester");
        user.setEmail("testing@test.com");
        user.setPassword("pass");
    }

    @Test
    public void authUserSuccess(){
        when(userService.getUserByUsername("Tester")).thenReturn(Optional.of(user));
        String accessToken = jwtService.generateToken("Tester", 3000, TokenType.ACCESS);
        tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName("api/user/authUserTest")
                .execute()
                .errors()
                .verify()
                .path("authorisedUser.userInfo.id").entity(Integer.class).isEqualTo(user.getId())
                .path("authorisedUser.userInfo.username").entity(String.class).isEqualTo(user.getUsername())
                .path("authorisedUser.email").entity(String.class).isEqualTo(user.getEmail());
    }

    @Test
    public void authUserNotExist(){
        when(userService.getUserByUsername("Tester")).thenReturn(Optional.empty());
        String accessToken = jwtService.generateToken("Tester", 3000, TokenType.ACCESS);
        tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName("api/user/authUserTest")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.isEmpty()).isFalse();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }
}
