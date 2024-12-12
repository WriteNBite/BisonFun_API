package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import info.movito.themoviedbapi.model.core.TvSeries;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface TvSeriesMapper extends TmdbMapper{

    @Mapping(target = "id", expression = "java(null)")
    @Named("fromTvSeries")
    @Mapping(target = "externalIds.tmdbId", source = "id")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title.english", source = "name")
    @Mapping(target = "format", constant = "TV")
    @Mapping(target = "year", expression = "java(tmdbYear(tvSeries.getFirstAirDate()))")
    VideoContent.BasicInfo toBasicInfo(TvSeries tvSeries);

    @IterableMapping(qualifiedByName = "fromTvSeries")
    List<VideoContent.BasicInfo> toBasicInfoList(List<TvSeries> tvSeriesList);
}
