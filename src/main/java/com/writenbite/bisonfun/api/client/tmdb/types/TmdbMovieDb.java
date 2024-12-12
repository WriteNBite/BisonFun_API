package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import info.movito.themoviedbapi.model.movies.MovieDb;

public record TmdbMovieDb(MovieDb movieDb) implements RawVideoContent {
}
