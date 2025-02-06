package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbMovieVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.types.mapper.ExternalIdMapper;
import info.movito.themoviedbapi.model.movies.MovieDb;
import org.mapstruct.Mapper;

@Mapper(uses = {ExternalIdMapper.class})
public interface MovieDbMapper{

    default TmdbVideoContent toTmdbMovieVideoContent(MovieDb movie){
        return new TmdbMovieVideoContent(movie);
    }

}
