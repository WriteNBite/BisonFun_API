package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.database.entity.UserVideoContent;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {VideoContentMapper.class, UserVideoContentListStatusMapper.class, UserMapper.class, UserVideoContentListStatusMapper.class})
public interface UserVideoContentListElementMapper {

    @Named("fromEntity")
    UserVideoContentListElement fromEntity(UserVideoContent userVideoContent);

    @IterableMapping(qualifiedByName = "fromEntity")
    List<UserVideoContentListElement> fromEntities(List<UserVideoContent> userVideoContents);

}
