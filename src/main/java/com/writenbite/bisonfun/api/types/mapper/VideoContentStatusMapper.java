package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaStatus;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper
public interface VideoContentStatusMapper {

    @ValueMappings({
            @ValueMapping(source = "RELEASING", target = "ONGOING"),
            @ValueMapping(source = "HIATUS", target = "PAUSED"),
            @ValueMapping(source = "CANCELLED", target = "CANCELED")
    })
    VideoContentStatus fromAniListMediaStatus(AniListMediaStatus status);
}
