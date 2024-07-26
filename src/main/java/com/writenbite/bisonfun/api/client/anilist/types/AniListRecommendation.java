package com.writenbite.bisonfun.api.client.anilist.types;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;

import java.io.Serializable;

public record AniListRecommendation(
        AniListMedia mediaRecommendation
) implements Serializable {
}
