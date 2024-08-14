package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import org.mapstruct.Mapper;

import java.util.*;

@Mapper
public interface VideoContentFormatMapper {

    VideoContentFormat fromVideoContentType(VideoContentType type);

    VideoContentType toVideoContentType(VideoContentFormat format);

    default Collection<AniListMediaFormat> toAniListMediaFormat(List<VideoContentFormat> formats){
        Set<AniListMediaFormat> formatSet = new HashSet<>();
        for (VideoContentFormat format : formats){
            switch (format){
                case MOVIE -> formatSet.add(AniListMediaFormat.MOVIE);
                case TV -> {
                    formatSet.add(AniListMediaFormat.TV);
                    formatSet.add(AniListMediaFormat.TV_SHORT);
                    formatSet.add(AniListMediaFormat.ONA);
                    formatSet.add(AniListMediaFormat.OVA);
                }
                case SPECIAL -> formatSet.add(AniListMediaFormat.SPECIAL);
                case MUSIC -> formatSet.add(AniListMediaFormat.MUSIC);
            }
            if(formatSet.size() == AniListMediaFormat.values().length){
                break;
            }
        }
        return formatSet;
    }

}
