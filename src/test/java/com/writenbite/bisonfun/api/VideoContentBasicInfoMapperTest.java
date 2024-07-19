package com.writenbite.bisonfun.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.AniListApiResponse;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.TmdbApiResponse;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    private TmdbApiResponse parser;
    @Mock
    private AniListApiResponse aniListApiResponse;

    private String oppenheimer;
    private String demonSlayer;
    private String konosuba;

    @Autowired
    VideoContentMapper videoContentMapper;

    @BeforeEach
    public void setUp() throws IOException {
        Resource oppenheimerResource = new ClassPathResource("oppenheimer.json");
        oppenheimer = new String(Files.readAllBytes(Paths.get(oppenheimerResource.getURI())));
        Resource demonSlayerResource = new ClassPathResource("demon_slayer.json");
        demonSlayer = new String(Files.readAllBytes(Paths.get(demonSlayerResource.getURI())));
        Resource konosubaResource = new ClassPathResource("konosuba_3.json");
        konosuba = new String(Files.readAllBytes(Paths.get(konosubaResource.getURI())));
    }

    @Test
    public void testMovieMapping() throws JsonProcessingException {
        TmdbClient tmdbClient = new TmdbClient(parser, new ObjectMapper());
        when(parser.getMovieById(872585)).thenReturn(new JSONObject(oppenheimer));
        MovieDb movie = tmdbClient.parseMovieById(872585);
        VideoContent videoContent = videoContentMapper.fromMovieDb(movie);

        System.out.println(movie.toString());
        System.out.println(videoContent.toString());

        Assertions.assertEquals(872585, videoContent.getTmdbId());
        Assertions.assertEquals("Oppenheimer", videoContent.getTitle());
        Assertions.assertEquals(VideoContentCategory.MAINSTREAM, videoContent.getCategory());
        Assertions.assertEquals("tt15398776", videoContent.getImdbId());
    }

    @Test
    public void testTvMapping() throws JsonProcessingException {
        TmdbClient tmdbClient = new TmdbClient(parser, new ObjectMapper());
        when(parser.getShowById(85937)).thenReturn(new JSONObject(demonSlayer));
        TvSeriesDb tvSeriesDb = tmdbClient.parseShowById(85937);
        VideoContent videoContent = videoContentMapper.fromTvSeriesDb(tvSeriesDb);

        System.out.println(tvSeriesDb.toString());
        System.out.println(videoContent.toString());

        Assertions.assertEquals(85937, videoContent.getTmdbId());
        Assertions.assertEquals("Demon Slayer: Kimetsu no Yaiba", videoContent.getTitle());
        Assertions.assertEquals(VideoContentCategory.MAINSTREAM, videoContent.getCategory());
        Assertions.assertEquals("tt9335498", videoContent.getImdbId());
    }

    @Test
    public void testAnimeMapping() throws ContentNotFoundException, TooManyAnimeRequestsException {
        AniListClient aniListClient = new AniListClient(aniListApiResponse, new Gson());
        when(aniListApiResponse.getAnimeById(136804)).thenReturn(new JSONObject(konosuba));
        AniListMedia aniListMedia = aniListClient.parseAnimeById(136804);
        VideoContent videoContent = videoContentMapper.fromAniListMedia(aniListMedia);

        System.out.println(aniListMedia.toString());
        System.out.println(videoContent.toString());

        Assertions.assertEquals(136804, videoContent.getAniListId());
        Assertions.assertEquals("KONOSUBA -God's Blessing on This Wonderful World! 3", videoContent.getTitle());
        Assertions.assertEquals(VideoContentCategory.ANIME, videoContent.getCategory());
        Assertions.assertEquals(49458, videoContent.getMalId());
    }
}
