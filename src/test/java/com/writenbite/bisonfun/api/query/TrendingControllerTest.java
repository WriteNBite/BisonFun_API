package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
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
import com.writenbite.bisonfun.api.types.videocontent.*;
import org.instancio.Instancio;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@GraphQlTest(VideoContentController.class)
@Import({MapperConfig.class, GraphQlConfig.class})
public class TrendingControllerTest {

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

    private String trending;
    private List<VideoContent.BasicInfo> animeTrends;
    private List<VideoContent.BasicInfo> movieTrends;
    private List<VideoContent.BasicInfo> tvTrends;

    @BeforeEach
    public void setUpTrends() throws IOException {
        //Get trends
        Resource trendinResource = new ClassPathResource("graphql-test/api/manyTrendingTests.graphql");
        trending = new String(Files.readAllBytes(Paths.get(trendinResource.getURI())));
        //Trends
        animeTrends = getAnimeTrends();
        movieTrends = Instancio.ofList(VideoContent.BasicInfo.class).size(10).create();
        tvTrends = Instancio.ofList(VideoContent.BasicInfo.class).size(10).create();
    }

    private static List<VideoContent.BasicInfo> getAnimeTrends(){
        return List.of(
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 1"),
                        "Anime 1 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.TV,
                        2007,
                        new ExternalId(111, 112, 113, "tt0000114")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 2"),
                        "Anime 2 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2008,
                        new ExternalId(121, 122, 123, "tt0000124")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 3"),
                        "Anime 3 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.TV,
                        2009,
                        new ExternalId(131, 132, 133, "tt0000134")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 4"),
                        "Anime 4 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2010,
                        new ExternalId(141, 142, 143, "tt0000144")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 5"),
                        "Anime 5 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.TV,
                        2011,
                        new ExternalId(151, 152, 153, "tt0000154")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 6"),
                        "Anime 6 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2012,
                        new ExternalId(161, 162, 163, "tt0000164")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 7"),
                        "Anime 7 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.TV,
                        2013,
                        new ExternalId(171, 172, 173, "tt0000174")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 8"),
                        "Anime 8 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2014,
                        new ExternalId(181, 182, 183, "tt0000184")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 9"),
                        "Anime 9 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.TV,
                        2015,
                        new ExternalId(191, 192, 193, "tt0000194")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Anime 10"),
                        "Anime 10 URL",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2016,
                        new ExternalId(201, 202, 203, "tt0000204")
                )

        );
    }

    @Test
    public void trendsFound() throws ContentNotFoundException, TooManyAnimeRequestsException {
        when(animeService.getAnimeTrends(any())).thenReturn(animeTrends);
        when(mainstreamService.getTrends(VideoContentFormat.MOVIE)).thenReturn(movieTrends);
        when(mainstreamService.getTrends(VideoContentFormat.TV)).thenReturn(tvTrends);

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .verify();
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("anime_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> movieTrendsResult = response.path("movie_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> tvTrendsResult = response.path("tv_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        assertIterableEquals(animeTrends, animeTrendsResult);
        assertIterableEquals(movieTrends, movieTrendsResult);
        assertIterableEquals(tvTrends, tvTrendsResult);
    }

    @Test
    public void animeTrendsNotFound() throws ContentNotFoundException, TooManyAnimeRequestsException {
        when(animeService.getAnimeTrends(any())).thenReturn(List.of());
        when(mainstreamService.getTrends(VideoContentFormat.MOVIE)).thenReturn(movieTrends);
        when(mainstreamService.getTrends(VideoContentFormat.TV)).thenReturn(tvTrends);

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .verify();
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("anime_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> movieTrendsResult = response.path("movie_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> tvTrendsResult = response.path("tv_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        assertIterableEquals(List.of(), animeTrendsResult);
        assertIterableEquals(movieTrends, movieTrendsResult);
        assertIterableEquals(tvTrends, tvTrendsResult);
    }

    @Test
    public void movieTrendsNotFound() throws ContentNotFoundException, TooManyAnimeRequestsException {
        when(animeService.getAnimeTrends(any())).thenReturn(animeTrends);
        when(mainstreamService.getTrends(VideoContentFormat.MOVIE)).thenThrow(new ContentNotFoundException());
        when(mainstreamService.getTrends(VideoContentFormat.TV)).thenReturn(tvTrends);

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("anime_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        response.path("movie_trends")
                .valueIsNull();
        List<VideoContent.BasicInfo> tvTrendsResult = response.path("tv_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        assertIterableEquals(animeTrends, animeTrendsResult);
        assertIterableEquals(tvTrends, tvTrendsResult);
    }

    @Test
    public void tvTrendsNotFound() throws ContentNotFoundException, TooManyAnimeRequestsException {
        when(animeService.getAnimeTrends(any())).thenReturn(animeTrends);
        when(mainstreamService.getTrends(VideoContentFormat.MOVIE)).thenReturn(movieTrends);
        when(mainstreamService.getTrends(VideoContentFormat.TV)).thenThrow(new ContentNotFoundException());

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("anime_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> movieTrendsResult = response.path("movie_trends.trends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        response.path("tv_trends")
                .valueIsNull();
        assertIterableEquals(animeTrends, animeTrendsResult);
        assertIterableEquals(movieTrends, movieTrendsResult);
    }
}
