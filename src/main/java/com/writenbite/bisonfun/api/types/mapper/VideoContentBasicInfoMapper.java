package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.*;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaTitle;
import com.writenbite.bisonfun.api.types.videocontent.ExternalId;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import com.writenbite.bisonfun.api.types.builder.VideoContentBasicInfoBuilder;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.*;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(uses = AniListMediaCoverImageMapper.class)
public interface VideoContentBasicInfoMapper {

    @Mapping(target = "format", source = "type")
    @Mapping(target = "title.english", source = "title")
    @Mapping(target = "externalIds", expression = "java(externalIds(videoContent))")
    VideoContent.BasicInfo fromVideoContentDb(com.writenbite.bisonfun.api.database.entity.VideoContent videoContent);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "externalIds", expression = "java(aniListExternalId(anime.id(), anime.idMal()))")
    @Mapping(target = "category", constant = "ANIME")
    @Mapping(source = "coverImage", target = "poster")
    @Mapping(target = "title.english", source = "title", qualifiedByName = "animeEnglishTitle")
    @Mapping(target = "format", expression = "java(animeFormat(anime.format()))")
    @Mapping(target = "year", source = "startDate.year")
    VideoContent.BasicInfo fromAniListMedia(AniListMedia anime);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "externalIds", expression = "java(tmdbExternalId(movie.getId(), movie.getImdbID()))")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title.english", source = "title")
    @Mapping(target = "format", constant = "MOVIE")
    @Mapping(target = "year", expression = "java(tmdbYear(movie.getReleaseDate()))")
    VideoContent.BasicInfo fromMovieDb(MovieDb movie);

    @Mapping(target = "id", expression = "java(null)")
    @Named("fromMovie")
    @Mapping(target = "title.english", source = "title")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "format", constant = "MOVIE")
    @Mapping(target = "year", expression = "java(tmdbYear(movie.getReleaseDate()))")
    @Mapping(target = "externalIds.tmdbId", source = "id")
    VideoContent.BasicInfo fromMovie(Movie movie);
    @IterableMapping(qualifiedByName = "fromMovie")
    List<VideoContent.BasicInfo> fromMovies(List<Movie> movies);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "externalIds", expression = "java(tmdbExternalId(tv.getId(), tv.getExternalIds().getImdbId()))")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title.english", source = "name")
    @Mapping(target = "format", constant = "TV")
    @Mapping(target = "year", expression = "java(tmdbYear(tv.getFirstAirDate()))")
    VideoContent.BasicInfo fromTvSeriesDb(TvSeriesDb tv);

    @Mapping(target = "id", expression = "java(null)")
    @Named("fromTvSeries")
    @Mapping(target = "externalIds.tmdbId", source = "id")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title.english", source = "name")
    @Mapping(target = "format", constant = "TV")
    @Mapping(target = "year", expression = "java(tmdbYear(tvSeries.getFirstAirDate()))")
    VideoContent.BasicInfo fromTvSeries(TvSeries tvSeries);
    @IterableMapping(qualifiedByName = "fromTvSeries")
    List<VideoContent.BasicInfo> fromTvSeriesList(List<TvSeries> tvSeriesList);

    default List<VideoContent.BasicInfo> fromAniListRecommendations(AniListRecommendationConnection aniListRecommendationConnection){
        return aniListRecommendationConnection.nodes()
                .stream()
                .map(AniListRecommendation::mediaRecommendation)
                .map(this::fromAniListMedia)
                .collect(Collectors.toList());
    }

    default VideoContent.BasicInfo fromModels(com.writenbite.bisonfun.api.database.entity.VideoContent videoContentDb, AniListMedia anime, MovieDb movie, TvSeriesDb tv) {
        VideoContent.BasicInfo tmdbVideoContent = null;
        VideoContentBasicInfoBuilder builder = new VideoContentBasicInfoBuilder();

        if(videoContentDb != null){
            builderFromVideoContentDb(builder, videoContentDb);
        }
        if (anime != null) {
            builderFromAniListMedia(builder, anime);
        }
        if(movie != null){
            tmdbVideoContent = fromMovieDb(movie);
        } else if (tv != null) {
            tmdbVideoContent = fromTvSeriesDb(tv);
        }

        if (tmdbVideoContent != null) {
            builderFromTmdbVideoContent(builder, tmdbVideoContent);
        }

        return builder
                .build();
    }

    default VideoContent.BasicInfo fromBasicModels(com.writenbite.bisonfun.api.database.entity.VideoContent videoContentDb, Movie movie, TvSeries tv, AniListMedia anime){
        VideoContent.BasicInfo tmdbVideoContent = null;
        VideoContentBasicInfoBuilder builder = new VideoContentBasicInfoBuilder();

        if(videoContentDb != null){
            builderFromVideoContentDb(builder, videoContentDb);
        }
        if (anime != null) {
            builderFromAniListMedia(builder, anime);
        }
        if(movie != null){
            tmdbVideoContent = fromMovie(movie);
        } else if (tv != null) {
            tmdbVideoContent = fromTvSeries(tv);
        }

        if (tmdbVideoContent != null) {
            builderFromTmdbVideoContent(builder, tmdbVideoContent);
        }

        return builder.build();
    }

    private VideoContentBasicInfoBuilder builderFromVideoContentDb(VideoContentBasicInfoBuilder builder, com.writenbite.bisonfun.api.database.entity.VideoContent videoContentDb){
        VideoContent.BasicInfo databaseVideoContent = fromVideoContentDb(videoContentDb);
        builder.id(databaseVideoContent.id())
                .title(databaseVideoContent.title())
                .poster(databaseVideoContent.poster())
                .year(databaseVideoContent.year())
                .category(databaseVideoContent.category())
                .videoContentFormat(databaseVideoContent.format());
        builder.setExternalId(databaseVideoContent.externalIds());
        return builder;
    }
    private VideoContentBasicInfoBuilder builderFromAniListMedia(VideoContentBasicInfoBuilder builder, AniListMedia anime){
        VideoContent.BasicInfo animeVideoContent = fromAniListMedia(anime);

        builder.title(animeVideoContent.title())
                .poster(animeVideoContent.poster())
                .year(animeVideoContent.year())
                .category(animeVideoContent.category())
                .videoContentFormat(animeVideoContent.format());
        builder.getExternalId().aniListId(animeVideoContent.externalIds().aniListId())
                .malId(animeVideoContent.externalIds().malId());
        return builder;
    }
    private VideoContentBasicInfoBuilder builderFromTmdbVideoContent(VideoContentBasicInfoBuilder builder, VideoContent.BasicInfo tmdbVideoContent){
        builder.titleIfEmptyOrNull(tmdbVideoContent.title())
                .posterIfEmptyOrNull(tmdbVideoContent.poster())
                .yearIfEmptyOrNull(tmdbVideoContent.year())
                .categoryIfNull(tmdbVideoContent.category())
                .videoContentFormatIfNull(tmdbVideoContent.format());
        builder.getExternalId().tmdbId(tmdbVideoContent.externalIds().tmdbId())
                .imdbId(tmdbVideoContent.externalIds().imdbId());
        return builder;
    }

    default ExternalId externalIds(com.writenbite.bisonfun.api.database.entity.VideoContent videoContent){
        return new ExternalId(videoContent.getAniListId(), videoContent.getTmdbId(), videoContent.getMalId(), videoContent.getImdbId());
    }

    default ExternalId tmdbExternalId(Integer tmdbId, String imdbId){
        return new ExternalId(null, tmdbId, null, imdbId);
    }

    default ExternalId aniListExternalId(Integer aniListId, Integer malId){
        return new ExternalId(aniListId, null, malId, null);
    }

    @Named("animeEnglishTitle")
    default String animeEnglishTitle(AniListMediaTitle title){
        if (title != null){
            if(title.english() != null){
                return title.english();
            } else if (title.romaji() != null) {
                return title.romaji();
            }
        }
        throw new IllegalArgumentException();
    }
    @Named("animeFormat")
    default VideoContentFormat animeFormat(AniListMediaFormat format){
        return switch (format){
            case SPECIAL -> VideoContentFormat.SPECIAL;
            case TV, ONA, OVA, TV_SHORT -> VideoContentFormat.TV;
            case MOVIE -> VideoContentFormat.MOVIE;
            case MUSIC -> VideoContentFormat.MUSIC;
            case null -> VideoContentFormat.UNKNOWN;
        };
    }

    @Named("tmdbYear")
    default int tmdbYear(String releaseDate){
        try{
            Date date = Date.valueOf(releaseDate);
            return date.toLocalDate().getYear();
        }catch (Exception e){
            return -1;
        }
    }
}
