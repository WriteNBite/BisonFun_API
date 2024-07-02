package com.writenbite.bisonfun.api.types.videocontent;

import java.time.LocalDate;
import java.util.List;

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
    ){}
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
