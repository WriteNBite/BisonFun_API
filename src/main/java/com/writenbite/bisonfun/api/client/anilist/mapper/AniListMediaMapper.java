package com.writenbite.bisonfun.api.client.anilist.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.*;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaExternalLink;
import com.writenbite.bisonfun.api.database.mapper.VideoContentCategoryMapper;
import com.writenbite.bisonfun.api.database.mapper.VideoContentTypeMapper;
import com.writenbite.bisonfun.api.types.mapper.ExternalIdMapper;
import com.writenbite.bisonfun.api.types.mapper.VideoContentStatusMapper;
import com.writenbite.bisonfun.api.types.videocontent.Network;
import com.writenbite.bisonfun.api.types.videocontent.Studio;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = {AniListMediaFormatMapper.class, AniListMediaTitleMapper.class, AniListMediaCoverImageMapper.class, ExternalIdMapper.class, VideoContentCategoryMapper.class, VideoContentTypeMapper.class, VideoContentStatusMapper.class})
public interface AniListMediaMapper {

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "externalIds", source = ".", qualifiedByName = "fromAniListMedia")
    @Mapping(target = "category", constant = "ANIME")
    @Mapping(source = "coverImage", target = "poster")
    @Mapping(target = "title.english", source = "title", qualifiedByName = "animeEnglishTitle")
    @Mapping(target = "year", source = "startDate.year")
    VideoContent.BasicInfo toBasicInfo(AniListMedia anime);

    @Mapping(target = "startDate", source = "startDate", qualifiedByName = "fromFuzzyDate")
    @Mapping(target = "endDate", source = "endDate", qualifiedByName = "fromFuzzyDate")
    @Mapping(target = "seasons", constant = "1")
    @Mapping(target = "studios", source = "studios", qualifiedByName = "fromAniListStudios")
    @Mapping(target = "networks", source = "externalLinks", qualifiedByName = "fromAniListExternalLinks")
    @Mapping(target = "recommendations", source = "recommendations", qualifiedByName = "animeMediaRecommendation")
    @Mapping(target = "meanScore", source = "averageScore", qualifiedByName = "averageScore")
    VideoContent.ExternalInfo toExternalInfo(AniListMedia anime);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "id", target = "aniListId")
    @Mapping(target = "category", constant = "ANIME")
    @Mapping(source = "idMal", target = "malId")
    @Mapping(source = "coverImage", target = "poster")
    @Mapping(target = "title", qualifiedByName = "animeTitle")
    @Mapping(target = "type", source = "format")
    @Mapping(source = "startDate.year", target = "year")
    com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(AniListMedia anime);

    @Named("fromFuzzyDate")
    default LocalDate fromFuzzyDate(AniListFuzzyDate fuzzyDate){
        if(fuzzyDate != null && fuzzyDate.year() != null && fuzzyDate.month() != null && fuzzyDate.day() != null) {
            return LocalDate.of(fuzzyDate.year(), fuzzyDate.month(), fuzzyDate.day());
        }else{
            return null;
        }
    }

    @Named("fromAniListStudios")
    default List<Studio> fromAniListStudios(AniListStudioConnection studioConnection){
        return studioConnection.nodes()
                .stream()
                .map(aniListStudio -> new Studio(aniListStudio.name()))
                .collect(Collectors.toList());
    }

    @Named("fromAniListExternalLinks")
    default List<Network> fromAniListExternalLinks(List<AniListMediaExternalLink> externalLinks){
        return externalLinks != null ? externalLinks.stream()
                .filter(externalLink -> externalLink.type() == AniListExternalLinkType.STREAMING)
                .map(externalLink -> new Network(externalLink.url(), externalLink.icon(), externalLink.color()))
                .collect(Collectors.toList()) : null;
    }

    @Named("averageScore")
    default Float averageScore(Integer averageScore){
        return averageScore != null ? averageScore/10f : 0;
    }

    @Named("animeMediaRecommendation")
    default List<VideoContent.BasicInfo> animeMediaRecommendation(AniListRecommendationConnection aniListRecommendationConnection){
        return aniListRecommendationConnection.nodes()
                .stream()
                .map(AniListRecommendation::mediaRecommendation)
                .map(this::toBasicInfo)
                .collect(Collectors.toList());
    }
}
