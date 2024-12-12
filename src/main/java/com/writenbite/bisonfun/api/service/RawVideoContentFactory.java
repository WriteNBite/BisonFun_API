package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.mapper.MovieDbMapper;
import com.writenbite.bisonfun.api.client.tmdb.mapper.TvSeriesDbMapper;
import com.writenbite.bisonfun.api.config.BasicInfoConfiguratorRegistry;
import com.writenbite.bisonfun.api.types.builder.*;
import com.writenbite.bisonfun.api.types.builder.configurator.VideoContentBasicInfoBuilderConfigurator;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@AllArgsConstructor
@Component
public class RawVideoContentFactory {

    private final BasicInfoConfiguratorRegistry configuratorRegistry;
    private final AniListMediaMapper aniListMediaMapper;
    private final MovieDbMapper movieDbMapper;
    private final TvSeriesDbMapper tvSeriesDbMapper;

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
        configurator.configure(builder, (T) rawVideoContent);
    }

    public com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(AniListMedia anime, MovieDb movie, TvSeriesDb tv) {
        if (anime != null) {
            com.writenbite.bisonfun.api.database.entity.VideoContent videoContent = aniListMediaMapper.toVideoContentDb(anime);

            com.writenbite.bisonfun.api.database.entity.VideoContent tmdbVideoContent = null;
            if(movie != null){
                tmdbVideoContent = movieDbMapper.toVideoContentDb(movie);
            } else if (tv != null) {
                tmdbVideoContent = tvSeriesDbMapper.toVideoContentDb(tv);
            }

            if(tmdbVideoContent != null){
                videoContent.setTmdbId(tmdbVideoContent.getTmdbId());
                videoContent.setImdbId(tmdbVideoContent.getImdbId());
                if(videoContent.getPoster().isEmpty()){
                    videoContent.setPoster(tmdbVideoContent.getPoster());
                }
            }
            return videoContent;

        } else if (movie != null) {
            return movieDbMapper.toVideoContentDb(movie);
        } else if (tv != null) {
            return tvSeriesDbMapper.toVideoContentDb(tv);
        } else {
            return null;
        }
    }
}
