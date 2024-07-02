package com.writenbite.bisonfun.api.client.tmdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbContentType;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TmdbClient {

    final
    TmdbApiResponse parser;
    final ObjectMapper objectMapper;

    @Autowired
    public TmdbClient(TmdbApiResponse parser, ObjectMapper objectMapper) {
        this.parser = parser;
        this.objectMapper = objectMapper;
    }

    public MovieDb parseMovieById(int tmdbId) throws JsonProcessingException {

        JSONObject root = parser.getMovieById(tmdbId);

        return objectMapper.readValue(root.toString(), MovieDb.class);
    }

    public TvSeriesDb parseShowById(int tmdbId) throws JsonProcessingException {
        JSONObject root = parser.getShowById(tmdbId);

        return objectMapper.readValue(root.toString(), TvSeriesDb.class);
    }

    public MovieResultsPage parseMovieList(String query, int page) throws JsonProcessingException {
        JSONObject root = parser.getTMDBList(query, TmdbContentType.MOVIE, page);

        return objectMapper.readValue(root.toString(), MovieResultsPage.class);
    }
    public MovieDb parseTmdbMovieByName(String name, int year) throws JsonProcessingException, ContentNotFoundException {
        JSONObject root = parser.getTMDBList(name, TmdbContentType.MOVIE, 1, year);
        JSONArray data = root.getJSONArray("results");

        int count = data.length();
        if(count <= 0){
            throw new ContentNotFoundException("Couldn't find tmdb movie by name '"+name+"'");
        }

        String stringContent = String.valueOf(data.getJSONObject(0));

        return objectMapper.readValue(stringContent, MovieDb.class);
    }
    public TvSeriesDb parseTmdbTvByName(String name, int year) throws ContentNotFoundException, JsonProcessingException {
        JSONObject root = parser.getTMDBList(name, TmdbContentType.TV, 1, year);
        JSONArray data = root.getJSONArray("results");

        int count = data.length();
        if(count <= 0){
            throw new ContentNotFoundException("Couldn't find tmdb tv by name '"+name+"'");
        }

        String stringContent = String.valueOf(data.getJSONObject(0));

        return objectMapper.readValue(stringContent, TvSeriesDb.class);
    }

    public MovieResultsPage parseMovieTrends() throws NoAccessException, JsonProcessingException {
        JSONObject movieTrends = parser.getMovieTrends();
        return objectMapper.readValue(movieTrends.toString(), MovieResultsPage.class);
    }
    public TvSeriesResultsPage parseTVList(String query, int page) throws JsonProcessingException {

        JSONObject root = parser.getTMDBList(query, TmdbContentType.TV, page);
        return objectMapper.readValue(root.toString(), TvSeriesResultsPage.class);
    }
    public TvSeriesResultsPage parseTVTrends() throws NoAccessException, JsonProcessingException {
        JSONObject trends = parser.getTvTrends();
        return objectMapper.readValue(trends.toString(), TvSeriesResultsPage.class);
    }

}
