package com.writenbite.bisonfun.api.client.anilist;

import com.writenbite.bisonfun.api.client.VideoContentModel;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StatusConverter implements Converter<AniListMediaStatus, VideoContentModel.Status> {
    @Override
    public VideoContentModel.Status convert(@NonNull AniListMediaStatus source) {
        return switch (source) {
            case FINISHED -> VideoContentModel.Status.RELEASED;
            case RELEASING -> VideoContentModel.Status.ONGOING;
            case NOT_YET_RELEASED -> VideoContentModel.Status.UPCOMING;
            case CANCELLED -> VideoContentModel.Status.CANCELED;
            case HIATUS -> VideoContentModel.Status.PAUSED;
        };
    }
}
