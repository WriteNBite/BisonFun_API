package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import info.movito.themoviedbapi.model.core.TvSeries;

public record TmdbTvSeries(TvSeries tvSeries) implements RawVideoContent {
}
