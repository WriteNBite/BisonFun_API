package com.writenbite.bisonfun.api.service.external;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import info.movito.themoviedbapi.model.core.AlternativeTitle;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MainstreamAnimeIntegrityValidator implements DataIntegrityValidator<TmdbVideoContent, AniListMedia> {

    private final AnimeService<AniListMedia, VideoContent> animeService;

    public MainstreamAnimeIntegrityValidator(AnimeService<AniListMedia, VideoContent> animeService) {
        this.animeService = animeService;
    }

    @Override
    public boolean dataIntegrityCheck(TmdbVideoContent tmdbVideoContent, AniListMedia aniListMedia) {
        if(tmdbVideoContent != null && aniListMedia != null) {
            return contentMatches(tmdbVideoContent, aniListMedia);
        } else if (tmdbVideoContent == null && aniListMedia == null) {
            throw new IllegalArgumentException("Could not find content in any source");
        }
        return true;
    }

    private boolean contentMatches(@NonNull TmdbVideoContent mainstream, @NonNull AniListMedia anime) {
        List<String> titles = new ArrayList<>(mainstream.getAlternativeTitles().stream().map(AlternativeTitle::getTitle).toList());
        titles.add(mainstream.getTitle());
        titles.add(mainstream.getOriginalTitle());
        for(String title : titles) {
            if(!animeService.isConflictingContent(anime, title)) {
                return true;
            }
        }
        return false;
    }
}
