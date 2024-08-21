package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.UserVideoContentListConnectionConfig;
import com.writenbite.bisonfun.api.controller.UserVideoContentController;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.service.UserVideoContentService;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@GraphQlTest(UserVideoContentController.class)
@Import({GraphQlConfig.class, UserVideoContentListConnectionConfig.class})
public class UserVideoContentControllerTest {

    @Autowired
    private GraphQlTester tester;
    @Autowired
    private UserVideoContentListConnection userVideoContentListConnection;
    @MockBean
    private UserVideoContentService userVideoContentService;
    @MockBean
    private UserService userService;

    @Test
    public void userVideoContentListFoundById() {
        when(userVideoContentService.userVideoContentList(eq(1), isNull(), any()))
                .thenReturn(userVideoContentListConnection);

        GraphQlTester.Response response = tester.documentName("api/userVideoContentListTest")
                .variable("userId", 1)
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
    public void userVideoContentListNoIdOrAuthorisation(){
        tester.documentName("api/userVideoContentListTest")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.isEmpty()).isFalse();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("BAD_REQUEST");
                });
    }

    @Test
    public void userVideoContentFound() throws ContentNotFoundException {
        UserVideoContentListElement element = userVideoContentListConnection.nodes()
                .stream()
                .filter(listElement -> listElement.user().id() == 1 && listElement.videoContent().id() == 2L)
                .findFirst()
                .orElse(null);
        when(userVideoContentService.userVideoContentListElement(1, 2L)).thenReturn(element);

        GraphQlTester.Response response = tester.documentName("api/userVideoContentTest")
                .execute();
        response.errors()
                .verify();
        UserVideoContentListElement actualElement = response.path("userVideoContent")
                .entity(UserVideoContentListElement.class)
                .get();
        assertEquals(element, actualElement);
    }

    @Test
    public void userVideoContentNotFound() throws ContentNotFoundException {
        when(userVideoContentService.userVideoContentListElement(1, 2L)).thenThrow(new ContentNotFoundException());

        tester.documentName("api/userVideoContentTest")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }
}
