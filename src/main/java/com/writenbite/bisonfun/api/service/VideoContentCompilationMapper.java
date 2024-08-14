package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.types.videocontent.input.TmdbIdInput;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import org.mapstruct.Mapper;

@Mapper
public interface VideoContentCompilationMapper {
    default VideoContentIdInput fromCompilation(VideoContentCompilation fetchedContent){
        int aniListId = fetchedContent.aniListMedia() != null ? fetchedContent.aniListMedia().id() : -1;
        int tmdbId = -1;
        VideoContentFormat format = VideoContentFormat.UNKNOWN;
        if(fetchedContent.movieDb() != null){
            tmdbId = fetchedContent.movieDb().getId();
            format = VideoContentFormat.MOVIE;
        } else if (fetchedContent.tvSeriesDb() != null) {
            tmdbId = fetchedContent.tvSeriesDb().getId();
            format = VideoContentFormat.TV;
        }
        return new VideoContentIdInput(fetchedContent.videoContentDb() != null ? fetchedContent.videoContentDb().getId() : null, aniListId, new TmdbIdInput(tmdbId, format));
    }
}
