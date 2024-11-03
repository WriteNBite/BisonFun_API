package com.writenbite.bisonfun.api.database.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaTitle;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.types.mapper.AniListMediaCoverImageMapper;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.*;

import java.sql.Date;

@Mapper(uses = {VideoContentCategoryMapper.class, VideoContentTypeMapper.class, AniListMediaCoverImageMapper.class})
public interface VideoContentMapper {

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "id", target = "aniListId")
    @Mapping(target = "category", constant = "ANIME")
    @Mapping(source = "idMal", target = "malId")
    @Mapping(source = "coverImage", target = "poster")
    @Mapping(target = "title", qualifiedByName = "animeTitle")
    @Mapping(target = "type", source = "format")
    @Mapping(source = "startDate.year", target = "year")
    VideoContent fromAniListMedia(AniListMedia anime);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "id", target = "tmdbId")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(source = "imdbID", target = "imdbId")
    @Mapping(source = "posterPath", target = "poster")
    @Mapping(target = "type", constant = "MOVIE")
    @Mapping(target = "year", expression = "java(tmdbYear(movie.getReleaseDate()))")
    VideoContent fromMovieDb(MovieDb movie);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "id", target = "tmdbId")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(source = "externalIds.imdbId", target = "imdbId")
    @Mapping(source = "posterPath", target = "poster")
    @Mapping(source = "name", target = "title")
    @Mapping(target = "type", constant = "TV")
    @Mapping(target = "year", expression = "java(tmdbYear(tv.getFirstAirDate()))")
    VideoContent fromTvSeriesDb(TvSeriesDb tv);

    default VideoContent fromModels(AniListMedia anime, MovieDb movie, TvSeriesDb tv) {
        if (anime != null) {
            VideoContent videoContent = fromAniListMedia(anime);

            VideoContent tmdbVideoContent = null;
            if(movie != null){
                tmdbVideoContent = fromMovieDb(movie);
            } else if (tv != null) {
                tmdbVideoContent = fromTvSeriesDb(tv);
            }

            if(tmdbVideoContent != null){
                videoContent.setTmdbId(tmdbVideoContent.getTmdbId());
                videoContent.setImdbId(tmdbVideoContent.getImdbId());
                if(videoContent.getPoster().isEmpty()){
                    videoContent.setPoster(tmdbVideoContent.getPoster());
                }
            }
            return videoContent;

        } else if (movie != null) {
            return fromMovieDb(movie);
        } else if (tv != null) {
            return fromTvSeriesDb(tv);
        } else {
            return null;
        }
    }

    @Named("animeTitle")
    default String animeTitle(AniListMediaTitle title){
        if (title != null){
            if(title.english() != null){
                return title.english();
            } else if (title.romaji() != null) {
                return title.romaji();
            } else if (title.nativeTitle() != null) {
                return title.nativeTitle();
            }
        }
        throw new IllegalArgumentException();
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
