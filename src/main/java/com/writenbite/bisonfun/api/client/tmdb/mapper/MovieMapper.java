package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleMovieVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import info.movito.themoviedbapi.model.core.Movie;
import org.mapstruct.Mapper;

@Mapper
public interface MovieMapper{

    default TmdbSimpleVideoContent toSimpleMovieVideoContent(Movie movie){
        return new TmdbSimpleMovieVideoContent(movie);
    }
}
