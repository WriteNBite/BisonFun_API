package com.writenbite.bisonfun.api;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.AniListApiResponse;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaPage;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.client.tmdb.mapper.ResultsPageMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContentResultsPage;
import com.writenbite.bisonfun.api.config.ModelConfig;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTrending;
import info.movito.themoviedbapi.TmdbTvSeries;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import info.movito.themoviedbapi.tools.appendtoresponse.TvSeriesAppendToResponse;
import info.movito.themoviedbapi.tools.model.time.TimeWindow;
import kong.unirest.core.json.JSONObject;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
    private TmdbApi tmdbApi;

    @Autowired
    private AniListClient aniListClient;
    @Autowired
    private TmdbClient tmdbClient;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private Model<MovieDb> movieDbModel;
    @Autowired
    private Model<TvSeriesDb> tvSeriesDbModel;

    @Mock
    private TmdbTrending tmdbTrending;
    @Mock
    private TmdbMovies tmdbMovies;
    @Mock
    private TmdbTvSeries tmdbTvSeries;

    private String konosuba;
    private String animeTrends;
    @Autowired
    private ResultsPageMapper resultsPageMapper;

    @BeforeEach
    public void setUp() throws IOException {
        konosuba = readResourceFile("konosuba_3.json");
        animeTrends = readResourceFile("animeTrends.json");

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
    public void givenRedisCaching_whenMovieTrends_thenMovieTrendsReturnedFromCache() throws TmdbException, NoAccessException {
        MovieResultsPage instancioMovieTrends = ModelConfig.ofTmdbApi(MovieResultsPage.class).create();
//        ResultsPage<TmdbSimpleVideoContent> movieTrends = resultsPageMapper.fromMovieResultsPage(instancioMovieTrends);
        when(tmdbApi.getTrending()).thenReturn(tmdbTrending);
        when(tmdbTrending.getMovies(TimeWindow.WEEK, null)).thenReturn(instancioMovieTrends);

        TmdbVideoContentResultsPage trendsNotCached = tmdbClient.parseMovieTrends();
        TmdbVideoContentResultsPage cachedTrends = tmdbClient.parseMovieTrends();

        assertThat(trendsNotCached).isEqualTo(cachedTrends);
        verify(tmdbApi, times(1)).getTrending();
        TmdbVideoContentResultsPage movieTrendsFromCache = cacheManager.getCache("movieTrends").get(SimpleKey.EMPTY, TmdbVideoContentResultsPage.class);
        assertThat(movieTrendsFromCache).isNotNull();
        assertThat(movieTrendsFromCache).isEqualTo(cachedTrends);
    }

    @Test
    public void givenRedisCaching_whenTvTrends_thenTvTrendsReturnedFromCache() throws TmdbException, NoAccessException {
        TvSeriesResultsPage tvTrends = ModelConfig.ofTmdbApi(TvSeriesResultsPage.class).create();
        when(tmdbApi.getTrending()).thenReturn(tmdbTrending);
        when(tmdbTrending.getTv(TimeWindow.WEEK, null)).thenReturn(tvTrends);

        TmdbVideoContentResultsPage trendsNotCached = tmdbClient.parseTVTrends();
        TmdbVideoContentResultsPage cachedTrends = tmdbClient.parseTVTrends();

        assertThat(trendsNotCached).isEqualTo(cachedTrends);
        verify(tmdbApi, times(1)).getTrending();
        TmdbVideoContentResultsPage tvTrendsFromCache = cacheManager.getCache("tvTrends").get(SimpleKey.EMPTY, TmdbVideoContentResultsPage.class);
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
    public void givenRedisCaching_whenFindMovieById_thenMovieReturnedFromCache() throws TmdbException, ContentNotFoundException {
        final int MOVIE_ID = 872585;
        MovieDb movieDb = Instancio.of(movieDbModel).create();
        when(tmdbApi.getMovies()).thenReturn(tmdbMovies);
        when(tmdbMovies.getDetails(MOVIE_ID, null, MovieAppendToResponse.ALTERNATIVE_TITLES, MovieAppendToResponse.RECOMMENDATIONS, MovieAppendToResponse.KEYWORDS)).thenReturn(movieDb);

        TmdbVideoContent movieNotCached = tmdbClient.parseMovieById(MOVIE_ID);
        TmdbVideoContent cachedMovie = tmdbClient.parseMovieById(MOVIE_ID);

        assertThat(movieNotCached).isEqualTo(cachedMovie);
        verify(tmdbApi, times(1)).getMovies();
        Optional<TmdbVideoContent> movieFromCache = getCachedJSONObject("jsonMovie", MOVIE_ID, TmdbVideoContent.class);
        assertThat(movieFromCache).isPresent();
        assertThat(movieFromCache.get()).isEqualTo(cachedMovie);
    }

    @Test
    public void givenRedisCaching_whenFindShowById_thenShowReturnedFromCache() throws TmdbException, ContentNotFoundException {
        final int TV_ID = 85937;
        TvSeriesDb tvSeriesDb = Instancio.of(tvSeriesDbModel).create();
        when(tmdbApi.getTvSeries()).thenReturn(tmdbTvSeries);
        when(tmdbTvSeries.getDetails(TV_ID, null, TvSeriesAppendToResponse.ALTERNATIVE_TITLES, TvSeriesAppendToResponse.EXTERNAL_IDS, TvSeriesAppendToResponse.KEYWORDS, TvSeriesAppendToResponse.RECOMMENDATIONS)).thenReturn(tvSeriesDb);

        TmdbVideoContent tvNotCached = tmdbClient.parseShowById(TV_ID);
        TmdbVideoContent cachedTv = tmdbClient.parseShowById(TV_ID);

        assertThat(tvNotCached).isEqualTo(cachedTv);
        verify(tmdbApi, times(1)).getTvSeries();
        Optional<TmdbVideoContent> tvFromCache = getCachedJSONObject("jsonShow", TV_ID, TmdbVideoContent.class);
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