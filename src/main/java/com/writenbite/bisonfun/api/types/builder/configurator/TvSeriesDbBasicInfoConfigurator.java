package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.tmdb.mapper.TvSeriesDbMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbTvSeriesDb;
import org.springframework.stereotype.Component;

@Component
public class TvSeriesDbBasicInfoConfigurator extends TmdbBasicInfoConfigurator<TmdbTvSeriesDb> {
    public TvSeriesDbBasicInfoConfigurator(TvSeriesDbMapper tvSeriesDbMapper) {
        super(tmdbTvSeriesDb -> tvSeriesDbMapper.toBasicInfo(tmdbTvSeriesDb.tvSeriesDb()));
    }
}
