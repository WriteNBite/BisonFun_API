package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.types.user.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    User fromEntity(com.writenbite.bisonfun.api.database.entity.User userDb);
}
