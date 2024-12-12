package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.tmdb.mapper.MovieDbMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbMovieDb;
import org.springframework.stereotype.Component;

@Component
public class MovieDbBasicInfoConfigurator extends TmdbBasicInfoConfigurator<TmdbMovieDb>{

    public MovieDbBasicInfoConfigurator(MovieDbMapper movieDbMapper) {
        super(tmdbMovieDb -> movieDbMapper.toBasicInfo(tmdbMovieDb.movieDb()));
    }
}
