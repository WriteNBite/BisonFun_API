package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleTvSeriesVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import info.movito.themoviedbapi.model.core.TvSeries;
import org.mapstruct.Mapper;

@Mapper
public interface TvSeriesMapper{

    default TmdbSimpleVideoContent toTmdbSimpleTvSeriesVideoContent(TvSeries tvSeries){
        return new TmdbSimpleTvSeriesVideoContent(tvSeries);
    }
}
