package com.writenbite.bisonfun.api.client.anilist.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaTitle;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Optional;

@Mapper
public interface AniListMediaTitleMapper {

    @Named("animeEnglishTitle")
    default String animeEnglishTitle(AniListMediaTitle title) {
        return Optional.ofNullable(title)
                .map(t -> Optional.ofNullable(t.english())
                        .orElse(t.romaji()))
                .orElseThrow(() -> new IllegalArgumentException("Both English and Romaji titles are null"));
    }

    @Named("animeTitle")
    default String animeTitle(AniListMediaTitle title) {
        return Optional.ofNullable(title)
                .map(t -> t.english() != null ? t.english()
                        : t.romaji() != null ? t.romaji()
                        : t.nativeTitle())
                .orElseThrow(() -> new IllegalArgumentException("All title fields are null"));
    }
}
