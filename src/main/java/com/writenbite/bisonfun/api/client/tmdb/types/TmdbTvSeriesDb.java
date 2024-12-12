package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;

public record TmdbTvSeriesDb(TvSeriesDb tvSeriesDb) implements RawVideoContent {
}
