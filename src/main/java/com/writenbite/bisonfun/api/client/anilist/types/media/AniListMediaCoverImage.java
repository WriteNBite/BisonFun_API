package com.writenbite.bisonfun.api.client.anilist.types.media;

import java.io.Serializable;

public record AniListMediaCoverImage(
        String extraLarge,
        String large,
        String medium,
        String color
) implements Serializable {
}
