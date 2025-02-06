package com.writenbite.bisonfun.api.client.anilist.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper
public interface AniListMediaFormatMapper {

    @ValueMappings({
            @ValueMapping(source = "ONA", target = "TV"),
            @ValueMapping(source = "OVA", target = "TV"),
            @ValueMapping(source = "TV_SHORT", target = "TV"),
            @ValueMapping(source = "<NULL>", target = "UNKNOWN")
    })
    VideoContentFormat animeFormat(AniListMediaFormat format);
}
