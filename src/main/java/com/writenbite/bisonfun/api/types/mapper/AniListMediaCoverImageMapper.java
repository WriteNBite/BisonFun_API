package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaCoverImage;
import org.mapstruct.Mapper;

@Mapper
public interface AniListMediaCoverImageMapper {
    default String fromApi(AniListMediaCoverImage coverImage){
        if(coverImage != null){
            if(coverImage.extraLarge()!=null){
                return coverImage.extraLarge();
            }else if(coverImage.large()!=null){
                return coverImage.large();
            }else if(coverImage.medium()!=null){
                return coverImage.medium();
            }
        }
        return "";
    }
}
