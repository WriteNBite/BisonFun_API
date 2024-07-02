package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.database.entity.UserVideoContentStatus;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;
import org.mapstruct.Mapper;

@Mapper
public interface UserVideoContentListStatusMapper {
    default UserVideoContentStatus toUserVideoContentStatus(UserVideoContentListStatus listStatus){
        return switch (listStatus){
            case WATCHING -> UserVideoContentStatus.WATCHING;
            case PLANNING -> UserVideoContentStatus.PLANNED;
            case COMPLETED -> UserVideoContentStatus.COMPLETE;
            case DROPPED -> UserVideoContentStatus.DROPPED;
            case PAUSED -> UserVideoContentStatus.PAUSED;
        };
    }
    default UserVideoContentListStatus toApiStatus(UserVideoContentStatus listStatus){
        return switch (listStatus){
            case WATCHING -> UserVideoContentListStatus.WATCHING;
            case PLANNED -> UserVideoContentListStatus.PLANNING;
            case COMPLETE -> UserVideoContentListStatus.COMPLETED;
            case DROPPED -> UserVideoContentListStatus.DROPPED;
            case PAUSED -> UserVideoContentListStatus.PAUSED;
        };
    }
}
