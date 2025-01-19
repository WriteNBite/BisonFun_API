package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import com.writenbite.bisonfun.api.types.videocontent.*;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = {TmdbMapper.class, VideoContentFormatMapper.class})
public interface TmdbSimpleVideoContentMapper {

    TmdbMapper tmdbMapper = Mappers.getMapper(TmdbMapper.class);
    VideoContentFormatMapper formatMapper = Mappers.getMapper(VideoContentFormatMapper.class);

    @Named("fromSimpleVideoContentToBasicInfo")
    default VideoContent.BasicInfo toBasicInfo(TmdbSimpleVideoContent content){
        return new VideoContent.BasicInfo(
                null,
                new VideoContentTitle(content.getTitle()),
                content.getPosterPath(),
                VideoContentCategory.MAINSTREAM,
                formatMapper.fromVideoContentType(content.getVideoContentType()),
                tmdbMapper.tmdbYear(content.getReleaseDate()),
                new ExternalId(null, content.getTmdbId(), null, null)
        );
    }

    @IterableMapping(qualifiedByName = "fromSimpleVideoContentToBasicInfo")
    List<VideoContent.BasicInfo> toBasicInfoList(List<TmdbSimpleVideoContent> contents);

    default com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(TmdbSimpleVideoContent content) {
        com.writenbite.bisonfun.api.database.entity.VideoContent entity = new com.writenbite.bisonfun.api.database.entity.VideoContent();
        entity.setCategory(com.writenbite.bisonfun.api.database.entity.VideoContentCategory.MAINSTREAM);
        entity.setPoster(content.getPosterPath());
        entity.setTitle(content.getTitle());
        entity.setTmdbId(content.getTmdbId());
        entity.setType(content.getVideoContentType());
        entity.setYear(tmdbMapper.tmdbYear(content.getReleaseDate()));
        return entity;
    }
}
