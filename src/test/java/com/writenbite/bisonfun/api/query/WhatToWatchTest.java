package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.service.UserVideoContentService;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UserVideoContentListInput;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureHttpGraphQlTester
public class WhatToWatchTest {
    @Autowired
    private HttpGraphQlTester tester;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserVideoContentListConnection userVideoContentListConnection;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserVideoContentService userVideoContentService;

    private String accessToken;
    private User user;
    private static final String whatToWatchDocumentName = "api/whatToWatchTest";

    @BeforeEach
    public void tokenSetup() {
        user = new User();
        user.setId(1);
        user.setUsername("Tester");
        user.setEmail("testing@test.com");
        user.setPassword("pass");
        accessToken = jwtService.generateToken("AUser", 30000, TokenType.ACCESS);
    }

    @Test
    public void whatToWatchContentFound() {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        VideoContent.BasicInfo content = userVideoContentListConnection.nodes()
                .get(new Random().nextInt(userVideoContentListConnection.nodes().size()))
                .videoContent();
        when(userVideoContentService.whatToWatch(anyInt(), nullable(UserVideoContentListInput.class)))
                .thenReturn(content);
        GraphQlTester.Response response = tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName(whatToWatchDocumentName)
                .execute();
        response.errors()
                .verify();
        VideoContent.BasicInfo actualContent = response.path("whatToWatch")
                .entity(VideoContent.BasicInfo.class)
                .get();
        assertEquals(content, actualContent);
    }

    @Test
    public void whatToWatchContentNotFound() {
        // Mock the getUserByUsername method to return an Optional containing the user
        when(userService.getUserByUsername(anyString()))
                .thenReturn(Optional.of(user));

        // Mock the whatToWatch method to return null when called with any int and any UserVideoContentListInput
        when(userVideoContentService.whatToWatch(anyInt(), any(UserVideoContentListInput.class)))
                .thenReturn(null);

        // Execute the tester and verify the results
        tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName(whatToWatchDocumentName)
                .execute()
                .errors()
                .verify()
                .path("whatToWatch")
                .valueIsNull();
    }


    @Test
    public void whatToWatchUserNotFound() {
        when(userService.getUserByUsername(anyString()))
                .thenReturn(Optional.empty());
        tester.mutate()
                .header("Authorization", "Bearer " + accessToken)
                .build()
                .documentName(whatToWatchDocumentName)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.isEmpty()).isFalse();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }
}
