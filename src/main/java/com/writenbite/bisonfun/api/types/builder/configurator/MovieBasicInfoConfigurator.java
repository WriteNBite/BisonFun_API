package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.tmdb.mapper.MovieMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbMovie;
import org.springframework.stereotype.Component;

@Component
public class MovieBasicInfoConfigurator extends TmdbBasicInfoConfigurator<TmdbMovie>{
    public MovieBasicInfoConfigurator(MovieMapper movieMapper) {
        super(tmdbMovie -> movieMapper.toBasicInfo(tmdbMovie.movie()));
    }
}
