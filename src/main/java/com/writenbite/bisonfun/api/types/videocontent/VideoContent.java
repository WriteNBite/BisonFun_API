package com.writenbite.bisonfun.api.types.videocontent;

import com.writenbite.bisonfun.api.client.tmdb.TmdbPosterConfiguration;

import java.time.LocalDate;
import java.util.List;

import static com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory.MAINSTREAM;

public record VideoContent(
        BasicInfo basicInfo,
        ExternalInfo externalInfo
) {
    public record BasicInfo(
            Long id,
            VideoContentTitle title,
            String poster,
            VideoContentCategory category,
            VideoContentFormat format,
            Integer year,
            ExternalId externalIds
    ){
        @Override
        public String poster() {
            if (poster != null && (poster.startsWith("/") && category == MAINSTREAM)){
                return TmdbPosterConfiguration.DEFAULT.getUrl() + poster;
            }
            return poster;
        }
    }
    public record ExternalInfo(
            VideoContentStatus status,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Integer episodes,
            Integer seasons,
            Integer duration,
            List<String> genres,
            List<String> synonyms,
            Float meanScore,
            List<Studio> studios,
            List<Network> networks,
            List<BasicInfo> recommendations
    ) {
    }
}
