package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import org.mapstruct.Mapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper
public interface VideoContentFormatMapper {

    VideoContentFormat fromVideoContentType(VideoContentType type);

    VideoContentType toVideoContentType(VideoContentFormat format);

    default Collection<AniListMediaFormat> toAniListMediaFormat(List<VideoContentFormat> formats){
        return formats.stream()
                .flatMap(format -> switch (format) {
                    case MOVIE -> Stream.of(AniListMediaFormat.MOVIE);
                    case TV -> Stream.of(AniListMediaFormat.TV, AniListMediaFormat.TV_SHORT, AniListMediaFormat.ONA, AniListMediaFormat.OVA);
                    case SPECIAL -> Stream.of(AniListMediaFormat.SPECIAL);
                    case MUSIC -> Stream.of(AniListMediaFormat.MUSIC);
                    default -> Stream.empty();
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(AniListMediaFormat.class)));
    }

}
