package com.writenbite.bisonfun.api.client.anilist.types;

import java.util.List;

public record AniListRecommendationConnection(
        List<AniListRecommendation> nodes,
        AniListPageInfo pageInfo
) {
}
