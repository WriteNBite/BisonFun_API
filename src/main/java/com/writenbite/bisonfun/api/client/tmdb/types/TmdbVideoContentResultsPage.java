package com.writenbite.bisonfun.api.client.tmdb.types;

import java.io.Serializable;
import java.util.List;

public record TmdbVideoContentResultsPage(
        List<TmdbSimpleVideoContent> results,
        Integer page,
        Integer totalPages,
        Integer totalResults,
        int id
) implements Serializable {

}
