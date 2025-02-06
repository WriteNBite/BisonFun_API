package com.writenbite.bisonfun.api.database.mapper;

import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.types.mapper.ExternalIdMapper;
import org.mapstruct.*;

@Mapper(uses = {ExternalIdMapper.class})
public interface VideoContentMapper {

    @Mapping(target = "format", source = "type")
    @Mapping(target = "title.english", source = "title")
    @Mapping(target = "externalIds", source = ".", qualifiedByName = "fromVideoContentDb")
    com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo toBasicInfo(VideoContent videoContent);

}
