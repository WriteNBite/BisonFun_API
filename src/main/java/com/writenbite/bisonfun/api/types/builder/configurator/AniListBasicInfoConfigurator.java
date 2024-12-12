package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.types.builder.VideoContentBasicInfoBuilder;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import org.springframework.stereotype.Component;

@Component
public class AniListBasicInfoConfigurator extends VideoContentBasicInfoBuilderConfigurator<AniListMedia>{
    private final AniListMediaMapper aniListMediaMapper;
    private final static int PRIORITY = 1;

    public AniListBasicInfoConfigurator(AniListMediaMapper aniListMediaMapper) {
        this.aniListMediaMapper = aniListMediaMapper;
    }

    @Override
    public void configure(VideoContentBasicInfoBuilder builder, AniListMedia anime) {
        VideoContent.BasicInfo animeVideoContent = aniListMediaMapper.toBasicInfo(anime);

        builder.title(animeVideoContent.title())
                .poster(animeVideoContent.poster())
                .year(animeVideoContent.year())
                .category(animeVideoContent.category())
                .videoContentFormat(animeVideoContent.format());

        builder.getExternalId()
                .aniListId(animeVideoContent.externalIds().aniListId())
                .malId(animeVideoContent.externalIds().malId());
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
