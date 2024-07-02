package com.writenbite.bisonfun.api.types.videocontent;

import java.time.LocalDate;
import java.util.List;

public record VideoContentExternalInfo(
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
        List<VideoContent> recommendations
) {
}
