package com.writenbite.bisonfun.api.client.anilist.types.media;

import com.writenbite.bisonfun.api.client.anilist.types.AniListFuzzyDate;
import com.writenbite.bisonfun.api.client.anilist.types.AniListRecommendationConnection;
import com.writenbite.bisonfun.api.client.anilist.types.AniListStudioConnection;
import com.writenbite.bisonfun.api.service.RawVideoContent;

import java.io.Serializable;
import java.util.List;

public record AniListMedia(
        int id,
        Integer idMal,
        AniListMediaTitle title,
        AniListMediaFormat format,
        AniListMediaStatus status,
        String description,
        AniListFuzzyDate startDate,
        AniListFuzzyDate endDate,
        Integer seasonYear,
        Integer episodes,
        Integer duration,
        String countryOfOrigin,
        Boolean isLicensed,
        Integer updatedAt,
        AniListMediaCoverImage coverImage,
        String bannerImage,
        List<String> genres,
        List<String> synonyms,
        Integer averageScore,
        Integer meanScore,
        Integer popularity,
        Boolean isLocked,
        Integer trending,
        Integer favourites,
        List<AniListMediaTag> tags,
        AniListStudioConnection studios,
        List<AniListMediaExternalLink> externalLinks,
        AniListRecommendationConnection recommendations,
        String siteUrl
) implements Serializable, RawVideoContent {
}
