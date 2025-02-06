package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.tmdb.mapper.TmdbSimpleVideoContentMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import org.springframework.stereotype.Component;

@Component
public class TmdbSimpleVideoContentBasicInfoConfigurator extends TmdbBasicInfoConfigurator<TmdbSimpleVideoContent> {

    public TmdbSimpleVideoContentBasicInfoConfigurator(TmdbSimpleVideoContentMapper mapper) {
        super(mapper::toBasicInfo);
    }
}
