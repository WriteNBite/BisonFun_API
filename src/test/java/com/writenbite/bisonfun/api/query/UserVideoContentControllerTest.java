package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.controller.UserVideoContentController;
import com.writenbite.bisonfun.api.service.UserVideoContentService;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.User;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import com.writenbite.bisonfun.api.types.videocontent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@GraphQlTest(UserVideoContentController.class)
@Import(GraphQlConfig.class)
public class UserVideoContentControllerTest {

    @Autowired
    private GraphQlTester tester;

    @MockBean
    private UserVideoContentService userVideoContentService;

    private String userVideoContentListQuery;
    private String userVideoContentQuery;
    private String whatToWatchQuery;

    private Random random;

    private Connection<UserVideoContentListElement> userVideoContentListConnection;

    @BeforeEach
    public void setUpUserVideoContent() throws IOException {
        //Get userVideoContentList query
        Resource userVideoContentListQueryResource = new ClassPathResource("graphql-test/api/userVideoContentListTest.graphql");
        userVideoContentListQuery = new String(Files.readAllBytes(Paths.get(userVideoContentListQueryResource.getURI())));
        //Get userVideoContent query
        Resource userVideoContentQueryResource = new ClassPathResource("graphql-test/api/userVideoContentTest.graphql");
        userVideoContentQuery = new String(Files.readAllBytes(Paths.get(userVideoContentQueryResource.getURI())));
        //Get whatToWatch query
        Resource whatToWatchQueryResource = new ClassPathResource("graphql-test/api/whatToWatchTest.graphql");
        whatToWatchQuery = new String(Files.readAllBytes(Paths.get(whatToWatchQueryResource.getURI())));
        //Get Random
        random = new Random();
        //Generating user list
        User user = new User(1, "test@test.com", "tester");
        List<UserVideoContentListElement> userList = getMovies().stream()
                .map(movie -> new UserVideoContentListElement(user, movie, random.nextInt(11), random.nextInt(11), UserVideoContentListStatus.values()[random.nextInt(UserVideoContentListStatus.values().length)]))
                .toList();
        userVideoContentListConnection = new UserVideoContentListConnection(userList, new PageInfo(userList.size(), userList.size(), 1, 1, false));
    }

    private static List<VideoContent.BasicInfo> getMovies(){
        return List.of(
                new VideoContent.BasicInfo(
                        1L,
                        new VideoContentTitle("Title 1"),
                        "Poster 1 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2022,
                        new ExternalId(12, 13, 14, "tt0000015")
                ),
                new VideoContent.BasicInfo(
                        2L,
                        new VideoContentTitle("Title 2"),
                        "Poster 2 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2021,
                        new ExternalId(22, 23, 24, "tt0000025")
                ),
                new VideoContent.BasicInfo(
                        3L,
                        new VideoContentTitle("Title 3"),
                        "Poster 3 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2020,
                        new ExternalId(32, 33, 34, "tt0000035")
                ),
                new VideoContent.BasicInfo(
                        4L,
                        new VideoContentTitle("Title 4"),
                        "Poster 4 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2019,
                        new ExternalId(42, 43, 44, "tt0000045")
                ),
                new VideoContent.BasicInfo(
                        5L,
                        new VideoContentTitle("Title 5"),
                        "Poster 5 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2018,
                        new ExternalId(52, 53, 54, "tt0000055")
                ),
                new VideoContent.BasicInfo(
                        6L,
                        new VideoContentTitle("Title 6"),
                        "Poster 6 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2017,
                        new ExternalId(62, 63, 64, "tt0000065")
                ),
                new VideoContent.BasicInfo(
                        7L,
                        new VideoContentTitle("Title 7"),
                        "Poster 7 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2016,
                        new ExternalId(72, 73, 74, "tt0000075")
                ),
                new VideoContent.BasicInfo(
                        8L,
                        new VideoContentTitle("Title 8"),
                        "Poster 8 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2015,
                        new ExternalId(82, 83, 84, "tt0000085")
                ),
                new VideoContent.BasicInfo(
                        9L,
                        new VideoContentTitle("Title 9"),
                        "Poster 9 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2014,
                        new ExternalId(92, 93, 94, "tt0000095")
                ),
                new VideoContent.BasicInfo(
                        10L,
                        new VideoContentTitle("Title 10"),
                        "Poster 10 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2013,
                        new ExternalId(102, 103, 104, "tt0000105")
                )
        );
    }

    @Test
    public void userVideoContentListFoundById(){
        when(userVideoContentService.userVideoContentList(eq(1), isNull(), any()))
                .thenReturn(userVideoContentListConnection);

        GraphQlTester.Response response = tester.document(userVideoContentListQuery)
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
    public void userVideoContentFound() throws ContentNotFoundException {
        UserVideoContentListElement element = userVideoContentListConnection.nodes()
                .stream()
                .filter(listElement -> listElement.user().id() == 1 && listElement.videoContent().id() == 2L)
                .findFirst()
                .orElse(null);
        when(userVideoContentService.userVideoContentListElement(1, 2L)).thenReturn(element);

        GraphQlTester.Response response = tester.document(userVideoContentQuery)
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

        tester.document(userVideoContentQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void whatToWatchContentFound(){
        VideoContent.BasicInfo content = userVideoContentListConnection.nodes()
                .get(random.nextInt(userVideoContentListConnection.nodes().size()))
                .videoContent();
        when(userVideoContentService.whatToWatch(1, null))
                .thenReturn(content);

        GraphQlTester.Response response = tester.document(whatToWatchQuery)
                .execute();
        response.errors()
                .verify();
        VideoContent.BasicInfo actualContent = response.path("whatToWatch")
                .entity(VideoContent.BasicInfo.class)
                .get();
        assertEquals(content, actualContent);
    }

    @Test
    public void whatToWatchContentNotFound(){
        when(userVideoContentService.whatToWatch(1, null))
                .thenReturn(null);

        tester.document(whatToWatchQuery)
                .execute()
                .errors()
                .verify()
                .path("whatToWatch")
                .valueIsNull();
    }
}
