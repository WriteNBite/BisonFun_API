package com.writenbite.bisonfun.api.client;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaStatus;
import org.springframework.core.convert.converter.Converter;

public class AniListVideoContentModel implements VideoContentModel{
    private final AniListMedia media;
    private final Converter<AniListMediaStatus, Status> statusConverter;

    public AniListVideoContentModel(AniListMedia media, Converter<AniListMediaStatus, Status> statusConverter) {
        this.media = media;
        this.statusConverter = statusConverter;
    }

    @Override
    public int getEpisodes() {
        return media.episodes();
    }

    @Override
    public Status getStatus() {
        return statusConverter.convert(media.status());
    }
}
