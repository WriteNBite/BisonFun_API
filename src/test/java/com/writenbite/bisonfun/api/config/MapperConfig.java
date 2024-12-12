package com.writenbite.bisonfun.api.config;

import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaCoverImageMapper;
import com.writenbite.bisonfun.api.types.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    /**
     * User mapper for tests.
     *
     * @return created mapper
     */
    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }
    /**
     * AniListMediaCoverImage mapper for tests.
     *
     * @return created mapper
     */
    @Bean
    public AniListMediaCoverImageMapper aniListMediaCoverImageMapper(){
        return Mappers.getMapper(AniListMediaCoverImageMapper.class);
    }

}