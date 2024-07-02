package com.writenbite.bisonfun.api.client.anilist.types.media;

import com.writenbite.bisonfun.api.client.anilist.types.AniListExternalLinkType;

public record AniListMediaExternalLink(
        String url,
        String site,
        Integer siteId,
        AniListExternalLinkType type,
        String language,
        String color,
        String icon,
        String notes,
        boolean isDisabled
) {
}
