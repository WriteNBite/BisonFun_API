package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.mapper.TmdbVideoContentMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.config.BasicInfoConfiguratorRegistry;
import com.writenbite.bisonfun.api.types.builder.*;
import com.writenbite.bisonfun.api.types.builder.configurator.VideoContentBasicInfoBuilderConfigurator;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@AllArgsConstructor
@Component
public class RawVideoContentFactory {

    private final BasicInfoConfiguratorRegistry configuratorRegistry;
    private final AniListMediaMapper aniListMediaMapper;
    private final TmdbVideoContentMapper tmdbVideoContentMapper;

    public VideoContent.BasicInfo toBasicInfo(RawVideoContent... rawVideoContents){
        VideoContentBasicInfoBuilder builder = new VideoContentBasicInfoBuilder();
        if(rawVideoContents != null) {
            Map<VideoContentBasicInfoBuilderConfigurator<? extends RawVideoContent>, RawVideoContent> configurators = new HashMap<>();
            for (RawVideoContent rawVideoContent : rawVideoContents) {
                if(rawVideoContent != null) {
                    configurators.put(configuratorRegistry.getConfigurator(rawVideoContent.getClass()), rawVideoContent);
                }
            }
            configurators.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> configureBuilder(entry.getKey(), builder, entry.getValue()));
        }

        return builder.build();
    }

    private <T extends RawVideoContent> void configureBuilder(
            VideoContentBasicInfoBuilderConfigurator<T> configurator,
            VideoContentBasicInfoBuilder builder,
            RawVideoContent rawVideoContent
    ){
        if(configurator == null){
            return;
        }
        configurator.configure(builder, (T) rawVideoContent);
    }

    public com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(AniListMedia anime, TmdbVideoContent tmdbVideoContent) {
        if (anime != null) {
            com.writenbite.bisonfun.api.database.entity.VideoContent videoContent = aniListMediaMapper.toVideoContentDb(anime);

            com.writenbite.bisonfun.api.database.entity.VideoContent tmdbVideoContentEntity = tmdbVideoContentMapper.toVideoContentDb(tmdbVideoContent);

            if(tmdbVideoContentEntity != null){
                videoContent.setTmdbId(tmdbVideoContentEntity.getTmdbId());
                videoContent.setImdbId(tmdbVideoContentEntity.getImdbId());
                if(videoContent.getPoster().isEmpty()){
                    videoContent.setPoster(tmdbVideoContentEntity.getPoster());
                }
            }
            return videoContent;

        } else if (tmdbVideoContent != null) {
            return tmdbVideoContentMapper.toVideoContentDb(tmdbVideoContent);
        } else {
            return null;
        }
    }

}
