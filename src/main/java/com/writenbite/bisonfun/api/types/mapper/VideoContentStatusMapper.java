package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaStatus;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentStatus;
import org.mapstruct.Mapper;

@Mapper
public interface VideoContentStatusMapper {

    default VideoContentStatus fromAniListMediaStatus(AniListMediaStatus status){
        return switch (status){
            case FINISHED -> VideoContentStatus.FINISHED;
            case RELEASING -> VideoContentStatus.ONGOING;
            case NOT_YET_RELEASED -> VideoContentStatus.NOT_YET_RELEASED;
            case CANCELLED -> VideoContentStatus.CANCELED;
            case HIATUS -> VideoContentStatus.PAUSED;
        };
    }
}
