package com.writenbite.bisonfun.api.client.anilist.types.media;

import java.io.Serializable;

public record AniListMediaTag(
        String name,
        String description,
        String category,
        Integer rank,
        boolean isGeneralSpoiler,
        boolean isMediaSpoiler
) implements Serializable {
}
