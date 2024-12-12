package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.database.mapper.VideoContentTypeMapper;
import com.writenbite.bisonfun.api.types.mapper.ExternalIdMapper;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {ExternalIdMapper.class, VideoContentCategory.class, VideoContentTypeMapper.class})
public interface TvSeriesDbMapper extends TmdbMapper{

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "externalIds", source = ".", qualifiedByName = "fromTvSeriesDb")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title.english", source = "name")
    @Mapping(target = "format", constant = "TV")
    @Mapping(target = "year", expression = "java(tmdbYear(tv.getFirstAirDate()))")
    VideoContent.BasicInfo toBasicInfo(TvSeriesDb tv);

    @Mapping(target = "status", source = "status", qualifiedByName = "fromTmdbStatus")
    @Mapping(target = "description", source = "overview")
    @Mapping(target = "startDate", source = "firstAirDate", qualifiedByName = "fromTmdbDate")
    @Mapping(target = "endDate", source = "lastAirDate", qualifiedByName = "fromTmdbDate")
    @Mapping(target = "episodes", source = "numberOfEpisodes")
    @Mapping(target = "seasons", expression = "java(tv.getSeasons().size())")
    @Mapping(target = "duration", source = "episodeRunTime", qualifiedByName = "tvSeriesDuration")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "fromTmdbGenres")
    @Mapping(target = "synonyms", expression = "java(fromTmdbSynonyms(tv.getAlternativeTitles().getResults(), tv.getProductionCountries(), tv.getOriginalName()))")
    @Mapping(target = "meanScore", source = "voteAverage")
    @Mapping(target = "studios", source = "productionCompanies", qualifiedByName = "fromTmdbProductionCompanies")
    @Mapping(target = "recommendations", source = "recommendations.results")
    VideoContent.ExternalInfo toExternalInfo(TvSeriesDb tv);

    TvSeries toTvSeries(TvSeriesDb tvSeriesDb);

    TvSeriesDb fromTvSeries(TvSeries tvSeries);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "tmdbId", source = "id")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "imdbId", source = "externalIds.imdbId")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "type", constant = "TV")
    @Mapping(target = "year", expression = "java(tmdbYear(tv.getFirstAirDate()))")
    com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(TvSeriesDb tv);
}
