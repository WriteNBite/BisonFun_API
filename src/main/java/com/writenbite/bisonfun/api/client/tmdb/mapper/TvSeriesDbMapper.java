package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbTvSeriesVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.database.mapper.VideoContentTypeMapper;
import com.writenbite.bisonfun.api.types.mapper.ExternalIdMapper;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.Mapper;

@Mapper(uses = {ExternalIdMapper.class, VideoContentCategory.class, VideoContentTypeMapper.class})
public interface TvSeriesDbMapper{
    default TmdbVideoContent toTmdbTvSeriesVideoContent(TvSeriesDb tv){
        return new TmdbTvSeriesVideoContent(tv);
    }
}
