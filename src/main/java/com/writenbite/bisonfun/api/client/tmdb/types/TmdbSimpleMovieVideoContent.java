package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import info.movito.themoviedbapi.model.core.Movie;

import java.util.List;

public class TmdbSimpleMovieVideoContent extends TmdbSimpleVideoContent {

    private final Movie movie;

    public TmdbSimpleMovieVideoContent(Movie movie) {
        this.movie = movie;
    }

    @Override
    public Integer getTmdbId() {
        return movie.getId();
    }

    @Override
    public Boolean isAdult() {
        return movie.getAdult();
    }

    @Override
    public String getBackdropPath() {
        return movie.getBackdropPath();
    }

    @Override
    public List<Integer> getGenreIds() {
        return movie.getGenreIds();
    }

    @Override
    public String getOriginalLanguage() {
        return movie.getOriginalLanguage();
    }

    @Override
    public String getOriginalTitle() {
        return movie.getOriginalTitle();
    }

    @Override
    public String getOverview() {
        return movie.getOverview();
    }

    @Override
    public String getTitle() {
        return movie.getTitle();
    }

    @Override
    public Double getPopularity() {
        return movie.getPopularity();
    }

    @Override
    public String getPosterPath() {
        return movie.getPosterPath();
    }

    @Override
    public String getReleaseDate() {
        return movie.getReleaseDate();
    }

    @Override
    public Double getVoteAverage() {
        return movie.getVoteAverage();
    }

    @Override
    public VideoContentType getVideoContentType() {
        return VideoContentType.MOVIE;
    }
}
