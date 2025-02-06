package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import com.writenbite.bisonfun.api.types.videocontent.input.TmdbVideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VideoContentCompilationMapper {

    VideoContentFormatMapper videoContentFormatMapper = Mappers.getMapper(VideoContentFormatMapper.class);

    default VideoContentIdInput fromCompilation(VideoContentCompilation fetchedContent){
        int aniListId = fetchedContent.aniListMedia() != null ? fetchedContent.aniListMedia().id() : -1;
        int tmdbId = -1;
        VideoContentFormat format = VideoContentFormat.UNKNOWN;
        if(fetchedContent.tmdbVideoContent() != null){
            tmdbId = fetchedContent.tmdbVideoContent().getTmdbId();
            format = videoContentFormatMapper.fromVideoContentType(fetchedContent.tmdbVideoContent().getVideoContentType());
        }
        return new VideoContentIdInput(fetchedContent.videoContentDb() != null ? fetchedContent.videoContentDb().getId() : null, aniListId, new TmdbVideoContentIdInput(tmdbId, format));
    }
}
