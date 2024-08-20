package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.types.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(source = "username", target = "username")
    User fromEntity(com.writenbite.bisonfun.api.database.entity.User userDb);
}
