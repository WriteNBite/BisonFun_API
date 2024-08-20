package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.MapperConfig;
import com.writenbite.bisonfun.api.controller.UserController;
import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@GraphQlTest(UserController.class)
@Import({MapperConfig.class, GraphQlConfig.class})
public class UserControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private UserService userService;

    private User userDb;

    @BeforeEach
    public void setUp() {
        userDb = new com.writenbite.bisonfun.api.database.entity.User();
        userDb.setId(1);
        userDb.setUsername("testuser");
        userDb.setEmail("testuser@example.com");
    }

    @Test
    public void testUserFoundById() {
        when(userService.getUserById(1)).thenReturn(Optional.of(userDb));

        graphQlTester.documentName("api/user/userTest")
                .execute()
                .path("user.userInfo.id").entity(Integer.class).isEqualTo(userDb.getId())
                .path("user.userInfo.username").entity(String.class).isEqualTo(userDb.getUsername());
    }

    @Test
    public void testUserNotFoundById() {
        when(userService.getUserById(1)).thenReturn(Optional.empty());

        graphQlTester.documentName("api/user/userTest")
                .execute()
                .path("user").valueIsNull();
    }

    @Test
    public void testUserFoundByUsername() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(userDb));

        graphQlTester.documentName("api/user/userByUsernameTest")
                .execute()
                .path("userByUsername.userInfo.id").entity(Integer.class).isEqualTo(userDb.getId())
                .path("userByUsername.userInfo.username").entity(String.class).isEqualTo(userDb.getUsername());
    }

    @Test
    public void testUserNotFoundByUsername() {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.empty());

        graphQlTester.documentName("api/user/userByUsernameTest")
                .execute()
                .path("userByUsername").valueIsNull();
    }
}
