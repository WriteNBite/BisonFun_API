package com.writenbite.bisonfun.api.database.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import org.mapstruct.*;

@Mapper
public interface VideoContentTypeMapper {

    @ValueMappings({
            // Map different names to TV
            @ValueMapping(source = "ONA", target = "TV"),
            @ValueMapping(source = "OVA", target = "TV"),
            @ValueMapping(source = "TV_SHORT", target = "TV"),

            // Default/null handling
            @ValueMapping(source = MappingConstants.NULL, target = "UNKNOWN"),
            @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "UNKNOWN")
    })
    VideoContentType fromAniListMediaFormat(AniListMediaFormat format);
}
