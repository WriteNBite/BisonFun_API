package com.writenbite.bisonfun.api.client.anilist.types;

public record AniListPageInfo(
        int perPage,
        int currentPage,
        int lastPage,
        boolean hasNextPage
) {
}
