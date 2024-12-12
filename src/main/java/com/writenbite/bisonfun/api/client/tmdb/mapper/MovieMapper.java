package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import info.movito.themoviedbapi.model.core.Movie;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface MovieMapper extends TmdbMapper{

    @Mapping(target = "id", expression = "java(null)")
    @Named("fromMovie")
    @Mapping(target = "title.english", source = "title")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "format", constant = "MOVIE")
    @Mapping(target = "year", expression = "java(tmdbYear(movie.getReleaseDate()))")
    @Mapping(target = "externalIds.tmdbId", source = "id")
    VideoContent.BasicInfo toBasicInfo(Movie movie);

    @IterableMapping(qualifiedByName = "fromMovie")
    List<VideoContent.BasicInfo> toBasicInfoList(List<Movie> movies);
}
