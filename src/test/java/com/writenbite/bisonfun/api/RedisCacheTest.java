package com.writenbite.bisonfun.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.AniListApiResponse;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaPage;
import com.writenbite.bisonfun.api.client.tmdb.TmdbApiResponse;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import kong.unirest.core.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RedisCacheTest {
    @MockBean
    private AniListApiResponse aniListApiResponse;
    @MockBean
    private TmdbApiResponse tmdbApiResponse;

    @Autowired
    private AniListClient aniListClient;
    @Autowired
    private TmdbClient tmdbClient;
    @Autowired
    private CacheManager cacheManager;

    private String oppenheimer;
    private String konosuba;
    private String demonSlayer;
    private String animeTrends;
    private String movieTrends;
    private String tvTrends;

    @BeforeEach
    public void setUp() throws IOException {
        oppenheimer = readResourceFile("oppenheimer.json");
        konosuba = readResourceFile("konosuba_3.json");
        demonSlayer = readResourceFile("demon_slayer.json");
        animeTrends = readResourceFile("animeTrends.json");
        movieTrends = readResourceFile("trends_movie.json");
        tvTrends = readResourceFile("trends_show.json");

        clearAllCaches();
    }

    @AfterEach
    public void tearDown(){
        clearAllCaches();
    }

    private <T> Optional<T> getCachedJSONObject(String cacheName, int id, Class<T> tClass){
        return Optional.ofNullable(cacheManager.getCache(cacheName)).map(c -> c.get(id, tClass));
    }

    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> Optional.ofNullable(cacheManager.getCache(cacheName)).ifPresent(Cache::clear));
    }

    private String readResourceFile(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }

    @Test
    public void givenRedisCaching_whenMovieTrends_thenMovieTrendsReturnedFromCache() throws NoAccessException, JsonProcessingException {
        when(tmdbApiResponse.getMovieTrends()).thenReturn(new JSONObject(movieTrends));

        MovieResultsPage trendsNotCached = tmdbClient.parseMovieTrends();
        MovieResultsPage cachedTrends = tmdbClient.parseMovieTrends();

        assertThat(trendsNotCached).isEqualTo(cachedTrends);
        verify(tmdbApiResponse, times(1)).getMovieTrends();
        MovieResultsPage movieTrendsFromCache = cacheManager.getCache("movieTrends").get(SimpleKey.EMPTY, MovieResultsPage.class);
        assertThat(movieTrendsFromCache).isNotNull();
        assertThat(movieTrendsFromCache).isEqualTo(cachedTrends);
    }

    @Test
    public void givenRedisCaching_whenTvTrends_thenTvTrendsReturnedFromCache() throws NoAccessException, JsonProcessingException {
        when(tmdbApiResponse.getTvTrends()).thenReturn(new JSONObject(tvTrends));

        TvSeriesResultsPage trendsNotCached = tmdbClient.parseTVTrends();
        TvSeriesResultsPage cachedTrends = tmdbClient.parseTVTrends();

        assertThat(trendsNotCached).isEqualTo(cachedTrends);
        verify(tmdbApiResponse, times(1)).getTvTrends();
        TvSeriesResultsPage tvTrendsFromCache = cacheManager.getCache("tvTrends").get(SimpleKey.EMPTY, TvSeriesResultsPage.class);
        assertThat(tvTrendsFromCache).isNotNull();
        assertThat(tvTrendsFromCache).isEqualTo(cachedTrends);
    }

    @Test
    public void givenRedisCaching_whenAnimeTrends_thenAnimeTrendsReturnedFromCache() throws NoAccessException, TooManyAnimeRequestsException {
        when(aniListApiResponse.getAnimeTrends()).thenReturn(new JSONObject(animeTrends));

        AniListPage<AniListMedia> trendsNotCached = aniListClient.parseAnimeTrends();
        AniListPage<AniListMedia> cachedTrends = aniListClient.parseAnimeTrends();

        assertThat(trendsNotCached.getPageInfo()).isEqualTo(cachedTrends.getPageInfo());
        assertThat(trendsNotCached.getList()).isEqualTo(cachedTrends.getList());
        verify(aniListApiResponse, times(1)).getAnimeTrends();
        AniListPage<AniListMedia> animeTrendsFromCache = cacheManager.getCache("animeTrends").get(SimpleKey.EMPTY, AniListMediaPage.class);
        assertThat(animeTrendsFromCache).isNotNull();
        assertThat(animeTrendsFromCache.getPageInfo()).isEqualTo(cachedTrends.getPageInfo());
        assertThat(animeTrendsFromCache.getList()).isEqualTo(cachedTrends.getList());
    }

    @Test
    public void givenRedisCaching_whenFindMovieById_thenMovieReturnedFromCache() throws JsonProcessingException {
        final int MOVIE_ID = 872585;
        when(tmdbApiResponse.getMovieById(MOVIE_ID)).thenReturn(new JSONObject(oppenheimer));

        MovieDb movieNotCached = tmdbClient.parseMovieById(MOVIE_ID);
        MovieDb cachedMovie = tmdbClient.parseMovieById(MOVIE_ID);

        assertThat(movieNotCached).isEqualTo(cachedMovie);
        verify(tmdbApiResponse, times(1)).getMovieById(MOVIE_ID);
        Optional<MovieDb> movieFromCache = getCachedJSONObject("jsonMovie", MOVIE_ID, MovieDb.class);
        assertThat(movieFromCache).isPresent();
        assertThat(movieFromCache.get()).isEqualTo(cachedMovie);
    }

    @Test
    public void givenRedisCaching_whenFindShowById_thenShowReturnedFromCache() throws JsonProcessingException {
        final int TV_ID = 85937;
        when(tmdbApiResponse.getShowById(TV_ID)).thenReturn(new JSONObject(demonSlayer));

        TvSeriesDb tvNotCached = tmdbClient.parseShowById(TV_ID);
        TvSeriesDb cachedTv = tmdbClient.parseShowById(TV_ID);

        assertThat(tvNotCached).isEqualTo(cachedTv);
        verify(tmdbApiResponse, times(1)).getShowById(TV_ID);
        Optional<TvSeriesDb> tvFromCache = getCachedJSONObject("jsonShow", TV_ID, TvSeriesDb.class);
        assertThat(tvFromCache).isPresent();
        assertThat(tvFromCache.get()).isEqualTo(cachedTv);
    }

    @Test
    public void givenRedisCaching_whenFindAnimeById_thenAnimeReturnedFromCache() throws ContentNotFoundException, TooManyAnimeRequestsException {
        final int ANIME_ID = 136804;
        when(aniListApiResponse.getAnimeById(ANIME_ID)).thenReturn(new JSONObject(konosuba));

        AniListMedia animeNotCached = aniListClient.parseAnimeById(ANIME_ID);
        AniListMedia cachedAnime = aniListClient.parseAnimeById(ANIME_ID);

        assertThat(animeNotCached).isEqualTo(cachedAnime);
        verify(aniListApiResponse, times(1)).getAnimeById(ANIME_ID);
        Optional<AniListMedia> animeFromCache = getCachedJSONObject("jsonAnime", ANIME_ID, AniListMedia.class);
        assertThat(animeFromCache).isPresent();
        assertThat(animeFromCache.get()).isEqualTo(cachedAnime);
    }

}