package com.writenbite.bisonfun.api.security;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper
public interface AuthorityMapper {
    @ValueMapping(target = "ACCESS", source = "ROLE_ACCESS")
    @ValueMapping(target = "REFRESH", source = "ROLE_REFRESH")
    TokenType toTokenType(Role role);

    @ValueMapping(target = "ROLE_ACCESS", source = "ACCESS")
    @ValueMapping(target = "ROLE_REFRESH", source = "REFRESH")
    @ValueMapping(target = MappingConstants.NULL, source = "NO_TOKEN")
    Role fromTokenType(TokenType tokenType);
}
