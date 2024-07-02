package com.writenbite.bisonfun.api.database.mapper;

import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import org.mapstruct.Mapper;

@Mapper
public interface VideoContentCategoryMapper {
    default com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory fromEntity(VideoContentCategory category){
        return switch (category){
            case MAINSTREAM -> com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory.MAINSTREAM;
            case ANIME -> com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory.ANIME;
        };
    }

    default VideoContentCategory fromApi(com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory category){
        return switch (category){
            case MAINSTREAM -> VideoContentCategory.MAINSTREAM;
            case ANIME -> VideoContentCategory.ANIME;
        };
    }
}
