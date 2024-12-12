package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.tmdb.mapper.TvSeriesMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbTvSeries;
import org.springframework.stereotype.Component;

@Component
public class TvSeriesBasicInfoConfigurator extends TmdbBasicInfoConfigurator<TmdbTvSeries>{
    public TvSeriesBasicInfoConfigurator(TvSeriesMapper tvSeriesMapper) {
        super(tmdbTvSeries -> tvSeriesMapper.toBasicInfo(tmdbTvSeries.tvSeries()));
    }
}
