package com.writenbite.bisonfun.api.client.anilist.types;

import java.io.Serializable;

public record AniListPageInfo(
        int perPage,
        int currentPage,
        int lastPage,
        boolean hasNextPage
) implements Serializable {
}
