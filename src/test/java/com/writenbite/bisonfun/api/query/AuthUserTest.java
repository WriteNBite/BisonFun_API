package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.service.UserVideoContentService;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"bisonfun.rate-limit.requests-per-second=4"})
@AutoConfigureHttpGraphQlTester
public class AuthUserTest {

    private User user;
    @Autowired
    private HttpGraphQlTester tester;
    @Autowired
    private JwtService jwtService;
    @MockBean
    private UserService userService;
    @MockBean
    private UserVideoContentService userVideoContentService;
    @Autowired
    private UserVideoContentListConnection userVideoContentListConnection;

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

    @Test
    public void authUserVideoContentListFound(){
        when(userVideoContentService.userVideoContentList(eq(33), isNull(), any()))
                .thenReturn(userVideoContentListConnection);
        when(userService.getUserByUsername("Tester")).thenReturn(Optional.of(user));
        String accessToken = jwtService.generateToken("Tester", 3000, TokenType.ACCESS);
        GraphQlTester.Response response = tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName("api/userVideoContentListTest")
                .execute();
        response.errors()
                .verify();
        PageInfo pageInfo = response
                .path("userVideoContentList.pageInfo")
                .entity(PageInfo.class)
                .get();
        List<UserVideoContentListElement> userVideoContentList = response
                .path("userVideoContentList.nodes")
                .entityList(UserVideoContentListElement.class)
                .get();
        assertIterableEquals(userVideoContentListConnection.nodes(), userVideoContentList);
        assertEquals(userVideoContentListConnection.pageInfo(), pageInfo);
    }

    @Test
    public void authUserVideoContentFound() throws ContentNotFoundException {
        UserVideoContentListElement element = userVideoContentListConnection.nodes()
                .stream()
                .filter(listElement -> listElement.user().id() == 1 && listElement.videoContent().id() == 2L)
                .findFirst()
                .orElse(null);
        when(userVideoContentService.userVideoContentListElement(33, 2L)).thenReturn(element);
        when(userService.getUserByUsername("Tester")).thenReturn(Optional.of(user));

        String accessToken = jwtService.generateToken("Tester", 3000, TokenType.ACCESS);
        GraphQlTester.Response response = tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName("api/userVideoContentTest")
                .execute();
        response.errors()
                .verify();
        UserVideoContentListElement actualElement = response.path("userVideoContent")
                .entity(UserVideoContentListElement.class)
                .get();
        assertEquals(element, actualElement);
    }
}
