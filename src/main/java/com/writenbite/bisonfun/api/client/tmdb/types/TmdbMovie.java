package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import info.movito.themoviedbapi.model.core.Movie;

public record TmdbMovie(Movie movie) implements RawVideoContent {
}
