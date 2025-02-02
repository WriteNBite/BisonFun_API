package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.MapperConfig;
import com.writenbite.bisonfun.api.controller.VideoContentController;
import com.writenbite.bisonfun.api.service.external.AnimeService;
import com.writenbite.bisonfun.api.service.external.MainstreamService;
import com.writenbite.bisonfun.api.service.external.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.service.VideoContentService;
import com.writenbite.bisonfun.api.service.search.VideoContentSearchService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@GraphQlTest(VideoContentController.class)
@Import({MapperConfig.class, GraphQlConfig.class})
public class SearchControllerTest {

    @Autowired
    private GraphQlTester tester;

    @MockBean
    private VideoContentService videoContentService;
    @MockBean
    private VideoContentSearchService videoContentSearchService;
    @MockBean
    private AnimeService<AniListMedia, com.writenbite.bisonfun.api.database.entity.VideoContent> animeService;
    @MockBean
    private MainstreamService<TmdbVideoContent, com.writenbite.bisonfun.api.database.entity.VideoContent, AniListMedia> mainstreamService;

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
        mainstreamSearchResult = new BasicInfoConnection(mainstreamList, new PageInfo.PageInfoBuilder().increaseTotal(mainstreamList.size()).setPerPage(mainstreamList.size()).setCurrentPageIfLess(1).setLastPageIfGreater(1).setHasNextPage(false).createPageInfo());
        //Anime Search Result
        List<VideoContent.BasicInfo> animeList = getAnimeBasicInfos();
        animeSearchResult = new BasicInfoConnection(animeList, new PageInfo.PageInfoBuilder().increaseTotal(animeList.size()).setPerPage(animeList.size() * 2).setCurrentPageIfLess(1).setLastPageIfGreater(2).setHasNextPage(true).createPageInfo());
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
    public void mainstreamSearchFound() throws TooManyAnimeRequestsException {
        when(videoContentSearchService.search(argThat( criteria ->
                "Test".equals(criteria.query()) &&
                        criteria.category() == VideoContentCategory.MAINSTREAM &&
                        criteria.formats() != null &&
                        criteria.page() == 1
        ))).thenReturn(mainstreamSearchResult);

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
    public void mainstreamSearchNotFound() throws TooManyAnimeRequestsException {
        PageInfo expectedPageInfo = new PageInfo.PageInfoBuilder().setPerPage(0).setCurrentPageIfLess(1).setLastPageIfGreater(1).setHasNextPage(false).createPageInfo();
        when(videoContentSearchService.search(argThat( criteria ->
                "Test".equals(criteria.query()) &&
                        criteria.category() == VideoContentCategory.MAINSTREAM &&
                        criteria.formats() != null &&
                        criteria.page() == 1
        ))).thenReturn(new BasicInfoConnection(List.of(), expectedPageInfo));

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
        assertIterableEquals(List.of(), mainstreamList);
        assertEquals(expectedPageInfo, pageInfo);
    }

    @Test
    public void animeSearchFound() throws TooManyAnimeRequestsException {
        when(videoContentSearchService.search(argThat( criteria ->
                "Test".equals(criteria.query()) &&
                        criteria.category() == VideoContentCategory.ANIME &&
                        criteria.formats() != null &&
                        criteria.page() == 1
        ))).thenReturn(animeSearchResult);

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
    public void animeSearchNotFound() throws TooManyAnimeRequestsException {
        PageInfo expectedPageInfo = new PageInfo.PageInfoBuilder().setPerPage(0).setCurrentPageIfLess(1).setLastPageIfGreater(1).setHasNextPage(false).createPageInfo();
        when(videoContentSearchService.search(argThat( criteria ->
                "Test".equals(criteria.query()) &&
                        criteria.category() == VideoContentCategory.ANIME &&
                        criteria.formats() != null &&
                        criteria.page() == 1
        ))).thenReturn(new BasicInfoConnection(List.of(), expectedPageInfo));

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
        assertIterableEquals(List.of(), animeList);
        assertEquals(expectedPageInfo, pageInfo);
    }
}
