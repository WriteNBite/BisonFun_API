package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.MapperConfig;
import com.writenbite.bisonfun.api.controller.VideoContentController;
import com.writenbite.bisonfun.api.service.VideoContentService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

@GraphQlTest(VideoContentController.class)
@Import({MapperConfig.class, GraphQlConfig.class})
public class TrendingControllerTest {

    @Autowired
    private GraphQlTester tester;

    @MockBean
    private VideoContentService videoContentService;

    private String trending;
    private List<VideoContent.BasicInfo> animeTrends;
    private List<VideoContent.BasicInfo> movieTrends;
    private List<VideoContent.BasicInfo> tvTrends;

    @BeforeEach
    public void setUpTrends() throws IOException {
        //Get trends
        Resource trendinResource = new ClassPathResource("graphql-test/api/trendingTest.graphql");
        trending = new String(Files.readAllBytes(Paths.get(trendinResource.getURI())));
        //Trends
        animeTrends = getAnimeTrends();
        movieTrends = getMovieTrends();
        tvTrends = getTvTrends();
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

    private static List<VideoContent.BasicInfo> getMovieTrends(){
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

    private static List<VideoContent.BasicInfo> getTvTrends(){
        return List.of(
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 1"),
                        "Tv Poster 1 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2002,
                        new ExternalId(2222, 2223, 2224, "tt0002225")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 2"),
                        "Tv Poster 2 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2003,
                        new ExternalId(2232, 2233, 2234, "tt0002235")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 3"),
                        "Tv Poster 3 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2004,
                        new ExternalId(2242, 2243, 2244, "tt0002245")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 4"),
                        "Tv Poster 4 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2005,
                        new ExternalId(2252, 2253, 2254, "tt0002255")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 5"),
                        "Tv Poster 5 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2006,
                        new ExternalId(2262, 2263, 2264, "tt0002265")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 6"),
                        "Tv Poster 6 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2007,
                        new ExternalId(2272, 2273, 2274, "tt0002275")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 7"),
                        "Tv Poster 7 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2008,
                        new ExternalId(2282, 2283, 2284, "tt0002285")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 8"),
                        "Tv Poster 8 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2009,
                        new ExternalId(2292, 2293, 2294, "tt0002295")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 9"),
                        "Tv Poster 9 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2010,
                        new ExternalId(2302, 2303, 2304, "tt0002305")
                ),
                new VideoContent.BasicInfo(
                        null,
                        new VideoContentTitle("Tv 10"),
                        "Tv Poster 10 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.TV,
                        2011,
                        new ExternalId(2312, 2313, 2314, "tt0002315")
                )
        );
    }

    @Test
    public void trendsFound() throws ContentNotFoundException {
        when(videoContentService.getAnimeTrends()).thenReturn(animeTrends);
        when(videoContentService.getMovieTrends()).thenReturn(movieTrends);
        when(videoContentService.getTvTrends()).thenReturn(tvTrends);

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .verify();
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("trendVideoContent.animeTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> movieTrendsResult = response.path("trendVideoContent.movieTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> tvTrendsResult = response.path("trendVideoContent.tvTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        assertIterableEquals(animeTrends, animeTrendsResult);
        assertIterableEquals(movieTrends, movieTrendsResult);
        assertIterableEquals(tvTrends, tvTrendsResult);
    }

    @Test
    public void animeTrendsNotFound() throws ContentNotFoundException {
        when(videoContentService.getAnimeTrends()).thenThrow(new ContentNotFoundException());
        when(videoContentService.getMovieTrends()).thenReturn(movieTrends);
        when(videoContentService.getTvTrends()).thenReturn(tvTrends);

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
        response.path("trendVideoContent.animeTrends")
                .valueIsNull();
        List<VideoContent.BasicInfo> movieTrendsResult = response.path("trendVideoContent.movieTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> tvTrendsResult = response.path("trendVideoContent.tvTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        assertIterableEquals(movieTrends, movieTrendsResult);
        assertIterableEquals(tvTrends, tvTrendsResult);
    }

    @Test
    public void movieTrendsNotFound() throws ContentNotFoundException {
        when(videoContentService.getAnimeTrends()).thenReturn(animeTrends);
        when(videoContentService.getMovieTrends()).thenThrow(new ContentNotFoundException());
        when(videoContentService.getTvTrends()).thenReturn(tvTrends);

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("trendVideoContent.animeTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        response.path("trendVideoContent.movieTrends")
                .valueIsNull();
        List<VideoContent.BasicInfo> tvTrendsResult = response.path("trendVideoContent.tvTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        assertIterableEquals(animeTrends, animeTrendsResult);
        assertIterableEquals(tvTrends, tvTrendsResult);
    }

    @Test
    public void tvTrendsNotFound() throws ContentNotFoundException {
        when(videoContentService.getAnimeTrends()).thenReturn(animeTrends);
        when(videoContentService.getMovieTrends()).thenReturn(movieTrends);
        when(videoContentService.getTvTrends()).thenThrow(new ContentNotFoundException());

        GraphQlTester.Response response = tester.document(trending)
                .execute();
        response.errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
        List<VideoContent.BasicInfo> animeTrendsResult = response.path("trendVideoContent.animeTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        List<VideoContent.BasicInfo> movieTrendsResult = response.path("trendVideoContent.movieTrends")
                .entityList(VideoContent.BasicInfo.class)
                .get();
        response.path("trendVideoContent.tvTrends")
                .valueIsNull();
        assertIterableEquals(animeTrends, animeTrendsResult);
        assertIterableEquals(movieTrends, movieTrendsResult);
    }
}
