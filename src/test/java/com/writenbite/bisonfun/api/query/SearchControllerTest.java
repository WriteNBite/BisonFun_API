package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.MapperConfig;
import com.writenbite.bisonfun.api.controller.VideoContentController;
import com.writenbite.bisonfun.api.service.VideoContentService;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.videocontent.*;
import com.writenbite.bisonfun.api.types.videocontent.output.BasicInfoConnection;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@GraphQlTest(VideoContentController.class)
@Import({MapperConfig.class, GraphQlConfig.class})
public class SearchControllerTest {

    @Autowired
    private GraphQlTester tester;

    @MockBean
    private VideoContentService videoContentService;

    private String mainstreamSearch;
    private String animeSearch;
    private Connection<VideoContent.BasicInfo> mainstreamSearchResult;
    private Connection<VideoContent.BasicInfo> animeSearchResult;

    @BeforeEach
    public void setUpSearch() throws IOException {
        //Get mainstreamSearch query
        Resource mainstreamSearchQueryResource = new ClassPathResource("graphql-test/api/mainstreamSearchTest.graphql");
        mainstreamSearch = new String(Files.readAllBytes(Paths.get(mainstreamSearchQueryResource.getURI())));
        //Get animeSearch query
        Resource animeSearchQueryResource = new ClassPathResource("graphql-test/api/animeSearchTest.graphql");
        animeSearch = new String(Files.readAllBytes(Paths.get(animeSearchQueryResource.getURI())));
        //Mainstream Search Result
        List<VideoContent.BasicInfo> mainstreamList = getMainstreamBasicInfos();
        mainstreamSearchResult = new BasicInfoConnection(mainstreamList, new PageInfo(mainstreamList.size(), mainstreamList.size(), 1, 1, false));
        //Anime Search Result
        List<VideoContent.BasicInfo> animeList = getAnimeBasicInfos();
        animeSearchResult = new BasicInfoConnection(animeList, new PageInfo(animeList.size(), animeList.size()*2, 1, 2, true));
    }

    private static List<VideoContent.BasicInfo> getMainstreamBasicInfos() {
        List<VideoContent.BasicInfo> mainstreamList = new ArrayList<>();
        mainstreamList.add(
                new VideoContent.BasicInfo(
                        1L,
                        new VideoContentTitle("Title 1"),
                        "Poster 1 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2022,
                        new ExternalId(12, 13, 14, "tt0000015")
                )
        );
        mainstreamList.add(
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Title 2"),
                        "Poster 2 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2002,
                        new ExternalId(22, 23, 24, "tt0000025")
                )
        );
        return mainstreamList;
    }

    private static List<VideoContent.BasicInfo> getAnimeBasicInfos() {
        List<VideoContent.BasicInfo> animeList = new ArrayList<>();
        animeList.add(
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 1"),
                        "Anime 1 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.TV,
                        2007,
                        new ExternalId(111, 112, 113, "tt0000114")
                )
        );
        animeList.add(
                new VideoContent.BasicInfo(
                        12L,
                        new VideoContentTitle("Anime 2"),
                        "Anime 2 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2014,
                        new ExternalId(121, 122, 123, "tt0000124")
                )
        );
        return animeList;
    }

    @Test
    public void mainstreamSearchFound() throws ContentNotFoundException {
        when(videoContentService.search(eq("Test"), eq(VideoContentCategory.MAINSTREAM), anyList(), eq(1))).thenReturn(mainstreamSearchResult);

        GraphQlTester.Response response = tester.document(mainstreamSearch)
                .execute();
        List<VideoContent.BasicInfo> mainstreamList = response.errors()
                .verify()
                .path("search.nodes")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        PageInfo pageInfo = response
                .errors()
                .verify()
                .path("search.pageInfo")
                .entity(PageInfo.class)
                .get();
        assertIterableEquals(mainstreamSearchResult.nodes(), mainstreamList);
        assertEquals(mainstreamSearchResult.pageInfo(), pageInfo);
    }

    @Test
    public void mainstreamSearchNotFound() throws ContentNotFoundException {
        when(videoContentService.search(eq("Test"), eq(VideoContentCategory.MAINSTREAM), anyList(), eq(1))).thenThrow(new ContentNotFoundException());

        tester.document(mainstreamSearch)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void animeSearchFound() throws ContentNotFoundException {
        when(videoContentService.search(eq("Test"), eq(VideoContentCategory.ANIME), anyList(), eq(1))).thenReturn(animeSearchResult);

        GraphQlTester.Response response = tester.document(animeSearch)
                .execute();
        List<VideoContent.BasicInfo> animeList = response.errors()
                .verify()
                .path("search.nodes")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        PageInfo pageInfo = response
                .errors()
                .verify()
                .path("search.pageInfo")
                .entity(PageInfo.class)
                .get();
        assertIterableEquals(animeSearchResult.nodes(), animeList);
        assertEquals(animeSearchResult.pageInfo(), pageInfo);
    }

    @Test
    public void animeSearchNotFound() throws ContentNotFoundException {
        when(videoContentService.search(eq("Test"), eq(VideoContentCategory.ANIME), anyList(), eq(1))).thenThrow(new ContentNotFoundException());

        tester.document(animeSearch)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }
}
