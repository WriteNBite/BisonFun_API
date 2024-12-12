package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import com.writenbite.bisonfun.api.types.builder.VideoContentBasicInfoBuilder;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;

import java.util.function.Function;

public abstract class TmdbBasicInfoConfigurator<T extends RawVideoContent> extends VideoContentBasicInfoBuilderConfigurator<T>{
    private final Function<T, VideoContent.BasicInfo> mapperFunction;
    private static final int PRIORITY = 2;

    public TmdbBasicInfoConfigurator(Function<T, VideoContent.BasicInfo> tmdbMapperFunction) {
        this.mapperFunction = tmdbMapperFunction;
    }

    @Override
    public void configure(VideoContentBasicInfoBuilder builder, T t) {
        if(t != null) {
            VideoContent.BasicInfo tmdbBasicInfo = mapperFunction.apply(t);

            builder.titleIfEmpty(tmdbBasicInfo.title())
                    .posterIfEmpty(tmdbBasicInfo.poster())
                    .yearIfEmpty(tmdbBasicInfo.year())
                    .categoryIfEmpty(tmdbBasicInfo.category())
                    .videoContentFormatIfEmpty(tmdbBasicInfo.format());
            builder.getExternalId().tmdbId(tmdbBasicInfo.externalIds().tmdbId())
                    .imdbId(tmdbBasicInfo.externalIds().imdbId());
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
