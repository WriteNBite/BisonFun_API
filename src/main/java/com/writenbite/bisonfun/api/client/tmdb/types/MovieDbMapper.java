package com.writenbite.bisonfun.api.client.tmdb.types;

import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.movies.MovieDb;
import org.mapstruct.Mapper;

@Mapper
public interface MovieDbMapper {

    Movie toMovie(MovieDb movieDb);

    MovieDb fromMovie(Movie movie);
}
