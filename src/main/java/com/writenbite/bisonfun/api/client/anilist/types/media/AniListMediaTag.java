package com.writenbite.bisonfun.api.client.anilist.types.media;

public record AniListMediaTag(
        String name,
        String description,
        String category,
        Integer rank,
        boolean isGeneralSpoiler,
        boolean isMediaSpoiler
) {
}
