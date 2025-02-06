package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.ExternalId;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.ExternalIds;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Optional;

@Mapper
public interface ExternalIdMapper {

    @Named("fromAniListMedia")
    default ExternalId fromAniListMedia(AniListMedia aniListMedia){
        return new ExternalId(aniListMedia.id(), null, aniListMedia.idMal(), null);
    }

    @Named("fromMovieDb")
    default ExternalId fromMovieDb(MovieDb movie){
        return new ExternalId(null, movie.getId(), null, movie.getImdbID());
    }

    @Named("fromTvSeriesDb")
    default ExternalId fromTvSeriesDb(TvSeriesDb tvSeries){
        ExternalIds tvSeriesExternalIds = tvSeries.getExternalIds();
        String imdbId = tvSeriesExternalIds != null ? tvSeriesExternalIds.getImdbId() : null;
        return new ExternalId(null, tvSeries.getId(), null, imdbId);
    }

    @Named("fromVideoContentDb")
    default ExternalId fromVideoContentDb(VideoContent videoContent){
        return Optional.ofNullable(videoContent)
                .map(content -> new ExternalId(
                        content.getAniListId(),
                        content.getTmdbId(),
                        content.getMalId(),
                        content.getImdbId()
                ))
                .orElse(new ExternalId(null, null, null, null));
    }
}
