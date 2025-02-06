package com.writenbite.bisonfun.api.client.tmdb;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.tmdb.mapper.ResultsPageMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.*;
import info.movito.themoviedbapi.*;
import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.core.responses.TmdbResponseException;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import info.movito.themoviedbapi.tools.appendtoresponse.TvSeriesAppendToResponse;
import info.movito.themoviedbapi.tools.model.time.TimeWindow;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class TmdbClient {

    private final TmdbApi tmdbApi;
    private final ResultsPageMapper resultsPageMapper;

    @Cacheable("jsonMovie")
    public TmdbVideoContent parseMovieById(int tmdbId) throws ContentNotFoundException {
        TmdbMovies movies = tmdbApi.getMovies();
        try {
            MovieDb moviedb = movies.getDetails(tmdbId, null, MovieAppendToResponse.ALTERNATIVE_TITLES, MovieAppendToResponse.RECOMMENDATIONS, MovieAppendToResponse.KEYWORDS);
            return new TmdbMovieVideoContent(moviedb);
        }  catch (TmdbResponseException e) {
            int httpResponse = e.getResponseCode().getHttpStatus();
            if(httpResponse == 404){
                throw new ContentNotFoundException();
            }
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Cacheable("jsonShow")
    public TmdbVideoContent parseShowById(int tmdbId) throws ContentNotFoundException {
        TmdbTvSeries tvSeries = tmdbApi.getTvSeries();
        try {
            TvSeriesDb tv = tvSeries.getDetails(tmdbId, null, TvSeriesAppendToResponse.ALTERNATIVE_TITLES, TvSeriesAppendToResponse.EXTERNAL_IDS, TvSeriesAppendToResponse.KEYWORDS, TvSeriesAppendToResponse.RECOMMENDATIONS);
            return new TmdbTvSeriesVideoContent(tv);
        } catch (TmdbResponseException e) {
            int httpResponse = e.getResponseCode().getHttpStatus();
            if(httpResponse == 404){
                throw new ContentNotFoundException();
            }
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public TmdbSimpleVideoContent parseTmdbMovieByName(String name, Integer year) throws ContentNotFoundException {
        TmdbSearch search = tmdbApi.getSearch();
        MovieResultsPage searchResult;
        try {
             searchResult = search.searchMovie(name, false, null, null, 1, null, String.valueOf(year));
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        List<Movie> movies = searchResult.getResults();
        if(movies.isEmpty()){
            throw new ContentNotFoundException("Couldn't find tmdb movie by name '"+name+"'");
        }

        return new TmdbSimpleMovieVideoContent(movies.getFirst());
    }
    public TmdbSimpleVideoContent parseTmdbTvByName(String name, Integer year) throws ContentNotFoundException {
        TmdbSearch search = tmdbApi.getSearch();
        TvSeriesResultsPage searchResult;
        try {
            searchResult = search.searchTv(name, null, false, null, 1, year);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        List<TvSeries> tvs = searchResult.getResults();
        if(tvs.isEmpty()){
            throw new ContentNotFoundException("Couldn't find tmdb tv by name '"+name+"'");
        }

        return new TmdbSimpleTvSeriesVideoContent(tvs.getFirst());
    }

    @Cacheable("movieTrends")
    public TmdbVideoContentResultsPage parseMovieTrends() throws NoAccessException {
        TmdbTrending trends = tmdbApi.getTrending();
        try {
            MovieResultsPage movieTrends = trends.getMovies(TimeWindow.WEEK, null);
            return resultsPageMapper.fromMovieResultsPage(movieTrends);
        } catch (TmdbResponseException e) {
            if(e.getResponseCode().getHttpStatus() >= 400){
                throw new NoAccessException(e.getMessage());
            }
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Cacheable("tvTrends")
    public TmdbVideoContentResultsPage parseTVTrends() throws NoAccessException {
        TmdbTrending trends = tmdbApi.getTrending();
        try {
            return resultsPageMapper.fromTvSeriesResultsPage(trends.getTv(TimeWindow.WEEK, null));
        } catch (TmdbResponseException e) {
            if(e.getResponseCode().getHttpStatus() >= 400){
                throw new NoAccessException(e.getMessage());
            }
            log.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public TmdbVideoContentResultsPage parseMovieList(String query, int page) {
        TmdbSearch search = tmdbApi.getSearch();
        try {
            MovieResultsPage movieSearchResults = search.searchMovie(query, false, null, null, page, null, null);
            return resultsPageMapper.fromMovieResultsPage(movieSearchResults);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public TmdbVideoContentResultsPage parseTVList(String query, int page) {
        TmdbSearch search = tmdbApi.getSearch();
        try {
            TvSeriesResultsPage searchResult = search.searchTv(query, null, false, null, page, null);
            return resultsPageMapper.fromTvSeriesResultsPage(searchResult);
        } catch (TmdbException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
