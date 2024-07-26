package com.writenbite.bisonfun.api.client.anilist.types.media;

import java.io.Serializable;

public record AniListMediaTitle(
        String romaji,
        String english,
        String nativeTitle
) implements Serializable {
}
