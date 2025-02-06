package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.service.external.tmdb.TmdbVideoContentIdInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {VideoContentFormatMapper.class})
public interface TmdbIdInputMapper {
    @Mapping(target = "type", source = "format")
    TmdbVideoContentIdInput fromInputType(com.writenbite.bisonfun.api.types.videocontent.input.TmdbVideoContentIdInput input);
}
