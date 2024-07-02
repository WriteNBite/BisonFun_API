package com.writenbite.bisonfun.api.client;

import info.movito.themoviedbapi.model.movies.MovieDb;

public class TmdbMovieVideoContentModel implements VideoContentModel{
    private final MovieDb movie;

    public TmdbMovieVideoContentModel(MovieDb movie) {
        this.movie = movie;
    }

    @Override
    public int getEpisodes() {
        return 1;
    }

    @Override
    public Status getStatus() {
        return switch (movie.getStatus()){
            case "Rumored" -> Status.RUMORED;
            case "Planned" -> Status.PLANNED;
            case "In Production", "Post Production" -> Status.UPCOMING;
            case "Released" -> Status.RELEASED;
            case "Canceled" -> Status.CANCELED;
            default -> Status.UNKNOWN;
        };
    }
}
