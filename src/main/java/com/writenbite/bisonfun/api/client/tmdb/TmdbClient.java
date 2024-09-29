package com.writenbite.bisonfun.api.client.tmdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.tmdb.types.MovieDbMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TvSeriesDbMapper;
import info.movito.themoviedbapi.*;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.core.responses.TmdbResponseException;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import info.movito.themoviedbapi.tools.appendtoresponse.TvSeriesAppendToResponse;
import info.movito.themoviedbapi.tools.model.time.TimeWindow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TmdbClient {
    private final TvSeriesDbMapper tvSeriesDbMapper;
    private final MovieDbMapper movieDbMapper;

    final TmdbApi tmdbApi;

    @Autowired
    public TmdbClient(TmdbApi tmdbApi,
                      MovieDbMapper movieDbMapper,
                      TvSeriesDbMapper tvSeriesDbMapper) {
        this.tmdbApi = tmdbApi;
        this.movieDbMapper = movieDbMapper;
        this.tvSeriesDbMapper = tvSeriesDbMapper;
    }
    @Cacheable("jsonMovie")
    public MovieDb parseMovieById(int tmdbId) throws ContentNotFoundException {
        TmdbMovies movies = tmdbApi.getMovies();
        try {
            return movies.getDetails(tmdbId, null, MovieAppendToResponse.ALTERNATIVE_TITLES, MovieAppendToResponse.RECOMMENDATIONS, MovieAppendToResponse.KEYWORDS);
        }  catch (TmdbResponseException e) {
            int httpResponse = e.getResponseCode().getHttpStatus();
            if(httpResponse == 404){
                throw new ContentNotFoundException();
            }
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }
    @Cacheable("jsonShow")
    public TvSeriesDb parseShowById(int tmdbId) throws ContentNotFoundException {
        TmdbTvSeries tvSeries = tmdbApi.getTvSeries();
        try {
            return tvSeries.getDetails(tmdbId, null, TvSeriesAppendToResponse.ALTERNATIVE_TITLES, TvSeriesAppendToResponse.EXTERNAL_IDS, TvSeriesAppendToResponse.KEYWORDS, TvSeriesAppendToResponse.RECOMMENDATIONS);
        } catch (TmdbResponseException e) {
            int httpResponse = e.getResponseCode().getHttpStatus();
            if(httpResponse == 404){
                throw new ContentNotFoundException();
            }
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }

    public MovieResultsPage parseMovieList(String query, int page) {
        TmdbSearch search = tmdbApi.getSearch();
        try {
            return search.searchMovie(query, false, null, null, page, null, null);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }
    public MovieDb parseTmdbMovieByName(String name, int year) throws JsonProcessingException, ContentNotFoundException {
        TmdbSearch search = tmdbApi.getSearch();
        MovieResultsPage searchResult;
        try {
             searchResult = search.searchMovie(name, false, null, null, 1, null, String.valueOf(year));
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }

        List<Movie> movies = searchResult.getResults();
        if(movies.isEmpty()){
            throw new ContentNotFoundException("Couldn't find tmdb movie by name '"+name+"'");
        }

        return movieDbMapper.fromMovie(movies.getFirst());
    }
    public TvSeriesDb parseTmdbTvByName(String name, int year) throws ContentNotFoundException, JsonProcessingException {
        TmdbSearch search = tmdbApi.getSearch();
        TvSeriesResultsPage searchResult;
        try {
            searchResult = search.searchTv(name, null, false, null, 1, year);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }

        List<TvSeries> tvs = searchResult.getResults();
        if(tvs.isEmpty()){
            throw new ContentNotFoundException("Couldn't find tmdb tv by name '"+name+"'");
        }

        return tvSeriesDbMapper.fromTvSeries(tvs.getFirst());
    }
    @Cacheable("movieTrends")
    public MovieResultsPage parseMovieTrends() throws NoAccessException {
        TmdbTrending trends = tmdbApi.getTrending();
        try {
            return trends.getMovies(TimeWindow.WEEK, null);
        } catch (TmdbResponseException e) {
            if(e.getResponseCode().getHttpStatus() >= 400){
                throw new NoAccessException(e.getMessage());
            }
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }
    public TvSeriesResultsPage parseTVList(String query, int page) {
        TmdbSearch search = tmdbApi.getSearch();
        try {
            return search.searchTv(query, null, false, null, page, null);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }
    @Cacheable("tvTrends")
    public TvSeriesResultsPage parseTVTrends() throws NoAccessException {
        TmdbTrending trends = tmdbApi.getTrending();
        try {
            return trends.getTv(TimeWindow.WEEK, null);
        } catch (TmdbResponseException e) {
            if(e.getResponseCode().getHttpStatus() >= 400){
                throw new NoAccessException(e.getMessage());
            }
            throw new RuntimeException(e);
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }

}
