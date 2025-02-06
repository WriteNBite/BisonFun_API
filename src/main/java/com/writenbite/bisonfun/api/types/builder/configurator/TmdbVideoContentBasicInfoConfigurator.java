package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.tmdb.mapper.TmdbVideoContentMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import org.springframework.stereotype.Component;

@Component
public class TmdbVideoContentBasicInfoConfigurator extends TmdbBasicInfoConfigurator<TmdbVideoContent> {

    public TmdbVideoContentBasicInfoConfigurator(TmdbVideoContentMapper tmdbVideoContentMapper) {
        super(tmdbVideoContentMapper::toBasicInfo);
    }
}
