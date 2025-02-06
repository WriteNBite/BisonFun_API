package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.types.videocontent.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {TmdbSimpleVideoContentMapper.class, TmdbMapper.class})
public interface TmdbVideoContentMapper {

    TmdbSimpleVideoContentMapper tmdbSimpleVideoContentMapper = Mappers.getMapper(TmdbSimpleVideoContentMapper.class);
    TmdbMapper tmdbMapper = Mappers.getMapper(TmdbMapper.class);

    default VideoContent.BasicInfo toBasicInfo(TmdbVideoContent content) {
        return new VideoContent.BasicInfo(
                null,
                new VideoContentTitle(content.getTitle()),
                content.getPosterPath(),
                VideoContentCategory.MAINSTREAM,
                VideoContentFormat.TV,
                tmdbMapper.tmdbYear(content.getReleaseDate()),
                new ExternalId(null, content.getTmdbId(), null, content.getImdbId().orElse(null))
        );
    }

    default VideoContent.ExternalInfo toExternalInfo(TmdbVideoContent content) {
        return new VideoContent.ExternalInfo(
                tmdbMapper.fromTmdbStatus(content.getStringStatus()),
                content.getOverview(),
                tmdbMapper.fromTmdbDate(content.getReleaseDate()),
                tmdbMapper.fromTmdbDate(content.getLastEpisodeToAir()),
                content.getEpisodes(),
                content.getSeasons(),
                content.getRuntime().orElse(null),
                tmdbMapper.fromTmdbGenres(content.getGenres()),
                tmdbMapper.fromTmdbSynonyms(content.getAlternativeTitles(), content.getProductionCountries(), content.getOriginalTitle()),
                content.getVoteAverage().floatValue(),
                tmdbMapper.fromTmdbProductionCompanies(content.getProductionCompanies()),
                tmdbMapper.toNetworkTypeList(content.getNetworks()),
                tmdbSimpleVideoContentMapper.toBasicInfoList(content.getRecommendations())
        );
    }

    default com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(TmdbVideoContent content) {
        com.writenbite.bisonfun.api.database.entity.VideoContent entity = new com.writenbite.bisonfun.api.database.entity.VideoContent();
        entity.setCategory(tmdbMapper.toEntityCategory(content));
        entity.setImdbId(content.getImdbId().orElse(null));
        entity.setPoster(content.getPosterPath());
        entity.setTitle(content.getTitle());
        entity.setTmdbId(content.getTmdbId());
        entity.setType(content.getVideoContentType());
        entity.setYear(tmdbMapper.tmdbYear(content.getReleaseDate()));
        return entity;
    }
}
