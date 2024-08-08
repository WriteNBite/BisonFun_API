package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.database.entity.UserVideoContentStatus;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper
public interface UserVideoContentListStatusMapper {
    @ValueMapping(target = "PLANNED", source = "PLANNING")
    @ValueMapping(target = "COMPLETE", source = "COMPLETED")
    UserVideoContentStatus toUserVideoContentStatus(UserVideoContentListStatus listStatus);

    @ValueMapping(target = "PLANNING", source = "PLANNED")
    @ValueMapping(target = "COMPLETED", source = "COMPLETE")
    UserVideoContentListStatus toApiStatus(UserVideoContentStatus listStatus);
}
