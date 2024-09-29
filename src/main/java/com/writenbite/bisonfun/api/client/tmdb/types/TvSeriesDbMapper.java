package com.writenbite.bisonfun.api.client.tmdb.types;

import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.Mapper;

@Mapper
public interface TvSeriesDbMapper {

    TvSeries toTvSeries(TvSeriesDb tvSeriesDb);

    TvSeriesDb fromTvSeries(TvSeries tvSeries);
}
