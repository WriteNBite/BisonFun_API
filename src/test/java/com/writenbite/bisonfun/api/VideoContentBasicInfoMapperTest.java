package com.writenbite.bisonfun.api;

import com.google.gson.Gson;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.AniListApiResponse;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.client.tmdb.mapper.TmdbVideoContentMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbMovieVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbTvSeriesVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import kong.unirest.core.json.JSONObject;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
public class VideoContentBasicInfoMapperTest {
    @Mock
    private AniListApiResponse aniListApiResponse;

    @MockBean
    private TmdbClient tmdbClient;

    private String konosuba;

    @Autowired
    AniListMediaMapper aniListMediaMapper;
    @Autowired
    private Model<MovieDb> movieDbModel;
    @Autowired
    private Model<TvSeriesDb> tvSeriesDbModel;
    @Autowired
    private TmdbVideoContentMapper tmdbVideoContentMapper;

    @BeforeEach
    public void setUp() throws IOException {
        Resource konosubaResource = new ClassPathResource("konosuba_3.json");
        konosuba = new String(Files.readAllBytes(Paths.get(konosubaResource.getURI())));
    }

    @Test
    public void testMovieMapping() throws ContentNotFoundException {
        TmdbVideoContent expected = new TmdbMovieVideoContent(Instancio.of(movieDbModel).create());
        when(tmdbClient.parseMovieById(expected.getTmdbId())).thenReturn(expected);
        TmdbVideoContent movie = tmdbClient.parseMovieById(expected.getTmdbId());
        VideoContent actualVideoContent = tmdbVideoContentMapper.toVideoContentDb(movie);

        Assertions.assertEquals(expected.getTmdbId(), actualVideoContent.getTmdbId());
        Assertions.assertEquals(expected.getTitle(), actualVideoContent.getTitle());
        Assertions.assertEquals(VideoContentCategory.MAINSTREAM, actualVideoContent.getCategory());
        Assertions.assertEquals(expected.getImdbId().get(), actualVideoContent.getImdbId());
    }

    @Test
    public void testTvMapping() throws ContentNotFoundException {
        TmdbVideoContent expected = new TmdbTvSeriesVideoContent(Instancio.of(tvSeriesDbModel).create());
        when(tmdbClient.parseShowById(expected.getTmdbId())).thenReturn(expected);
        TmdbVideoContent tvSeriesDb = tmdbClient.parseShowById(expected.getTmdbId());
        VideoContent actualVideoContent = tmdbVideoContentMapper.toVideoContentDb(tvSeriesDb);

        Assertions.assertEquals(expected.getTmdbId(), actualVideoContent.getTmdbId());
        Assertions.assertEquals(expected.getTitle(), actualVideoContent.getTitle());
        Assertions.assertEquals(VideoContentCategory.MAINSTREAM, actualVideoContent.getCategory());
        Assertions.assertEquals(expected.getImdbId().get(), actualVideoContent.getImdbId());
    }

    @Test
    public void testAnimeMapping() throws ContentNotFoundException, TooManyAnimeRequestsException {
        AniListClient aniListClient = new AniListClient(aniListApiResponse, new Gson());
        when(aniListApiResponse.getAnimeById(136804)).thenReturn(new JSONObject(konosuba));
        AniListMedia aniListMedia = aniListClient.parseAnimeById(136804);
        VideoContent videoContent = aniListMediaMapper.toVideoContentDb(aniListMedia);

        System.out.println(aniListMedia.toString());
        System.out.println(videoContent.toString());

        Assertions.assertEquals(136804, videoContent.getAniListId());
        Assertions.assertEquals("KONOSUBA -God's Blessing on This Wonderful World! 3", videoContent.getTitle());
        Assertions.assertEquals(VideoContentCategory.ANIME, videoContent.getCategory());
        Assertions.assertEquals(49458, videoContent.getMalId());
    }
}
