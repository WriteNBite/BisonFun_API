package com.writenbite.bisonfun.api;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

@Slf4j
@SpringBootTest
public class TmdbApiTest {
    @Autowired
    private TmdbApi tmdbApi;

    @Test
    public void simpleTest(){
        TmdbMovies movies = tmdbApi.getMovies();
        MovieDb movie = null;
        try {
            movie = movies.getDetails(519182, null, MovieAppendToResponse.ALTERNATIVE_TITLES, MovieAppendToResponse.EXTERNAL_IDS, MovieAppendToResponse.KEYWORDS, MovieAppendToResponse.RECOMMENDATIONS);
        } catch (TmdbException e) {
            log.error(e.getMessage(), e);
        }
        log.info(Objects.requireNonNull(movie).toString());
    }
}
