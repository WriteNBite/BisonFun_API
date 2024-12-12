package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.types.mapper.ExternalIdMapper;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.movies.MovieDb;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {ExternalIdMapper.class})
public interface MovieDbMapper extends TmdbMapper{

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "externalIds", source = ".", qualifiedByName = "fromMovieDb")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "title.english", source = "title")
    @Mapping(target = "format", constant = "MOVIE")
    @Mapping(target = "year", expression = "java(tmdbYear(movie.getReleaseDate()))")
    VideoContent.BasicInfo toBasicInfo(MovieDb movie);

    @Mapping(target = "status", source = "status", qualifiedByName = "fromTmdbStatus")
    @Mapping(target = "description", source = "overview")
    @Mapping(target = "startDate", source = "releaseDate", qualifiedByName = "fromTmdbDate")
    @Mapping(target = "endDate", source = "releaseDate", qualifiedByName = "fromTmdbDate")
    @Mapping(target = "episodes", constant = "1")
    @Mapping(target = "seasons", constant = "1")
    @Mapping(target = "duration", source = "runtime")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "fromTmdbGenres")
    @Mapping(target = "synonyms", expression = "java(fromTmdbSynonyms(movie.getAlternativeTitles().getTitles(), movie.getProductionCountries(), movie.getOriginalTitle()))")
    @Mapping(target = "meanScore", source = "voteAverage")
    @Mapping(target = "studios", source = "productionCompanies", qualifiedByName = "fromTmdbProductionCompanies")
    @Mapping(target = "recommendations", source = "recommendations.results")
    VideoContent.ExternalInfo toExternalInfo(MovieDb movie);

    Movie toMovie(MovieDb movieDb);

    MovieDb fromMovie(Movie movie);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "tmdbId", source = "id")
    @Mapping(target = "category", constant = "MAINSTREAM")
    @Mapping(target = "imdbId", source = "imdbID")
    @Mapping(target = "poster", source = "posterPath")
    @Mapping(target = "type", constant = "MOVIE")
    @Mapping(target = "year", expression = "java(tmdbYear(movie.getReleaseDate()))")
    com.writenbite.bisonfun.api.database.entity.VideoContent toVideoContentDb(MovieDb movie);
}
