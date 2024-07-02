package com.writenbite.bisonfun.api.types.mapper;

import com.writenbite.bisonfun.api.client.anilist.types.*;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaExternalLink;
import com.writenbite.bisonfun.api.types.videocontent.Network;
import com.writenbite.bisonfun.api.types.videocontent.Studio;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentStatus;
import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = {VideoContentBasicInfoMapper.class, VideoContentStatusMapper.class})
public interface VideoContentExternalInfoMapper {

    @Mapping(target = "startDate", expression = "java(fromFuzzyDate(anime.startDate()))")
    @Mapping(target = "endDate", expression = "java(fromFuzzyDate(anime.endDate()))")
    @Mapping(target = "seasons", constant = "1")
    @Mapping(target = "studios", expression = "java(fromAniListStudios(anime.studios()))")
    @Mapping(target = "networks", expression = "java(fromAniListExternalLinks(anime.externalLinks()))")
    @Mapping(target = "recommendations", source = "recommendations")
    @Mapping(target = "meanScore", source = "averageScore", qualifiedByName = "averageScore")
    VideoContent.ExternalInfo fromAniListMedia(AniListMedia anime);
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
    VideoContent.ExternalInfo fromMovieDb(MovieDb movie);

    @Mapping(target = "status", source = "status", qualifiedByName = "fromTmdbStatus")
    @Mapping(target = "description", source = "overview")
    @Mapping(target = "startDate", source = "firstAirDate", qualifiedByName = "fromTmdbDate")
    @Mapping(target = "endDate", source = "lastAirDate", qualifiedByName = "fromTmdbDate")
    @Mapping(target = "episodes", source = "numberOfEpisodes")
    @Mapping(target = "seasons", expression = "java(tv.getSeasons().size())")
    @Mapping(target = "duration", source = "episodeRunTime", qualifiedByName = "tvSeriesDuration")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "fromTmdbGenres")
    @Mapping(target = "synonyms", expression = "java(fromTmdbSynonyms(tv.getAlternativeTitles().getResults(), tv.getProductionCountries(), tv.getOriginalName()))")
    @Mapping(target = "meanScore", source = "voteAverage")
    @Mapping(target = "studios", source = "productionCompanies", qualifiedByName = "fromTmdbProductionCompanies")
    @Mapping(target = "recommendations", source = "recommendations.results")
    VideoContent.ExternalInfo fromTvSeriesDb(TvSeriesDb tv);

    default LocalDate fromFuzzyDate(AniListFuzzyDate fuzzyDate){
        if(fuzzyDate != null && fuzzyDate.year() != null && fuzzyDate.month() != null && fuzzyDate.day() != null) {
            return LocalDate.of(fuzzyDate.year(), fuzzyDate.month(), fuzzyDate.day());
        }else{
            return null;
        }
    }

    default List<Studio> fromAniListStudios(AniListStudioConnection studioConnection){
        return studioConnection.nodes()
                .stream()
                .map(aniListStudio -> new Studio(aniListStudio.name()))
                .collect(Collectors.toList());
    }

    default List<Network> fromAniListExternalLinks(List<AniListMediaExternalLink> externalLinks){
        return externalLinks != null ? externalLinks.stream()
                .filter(externalLink -> externalLink.type() == AniListExternalLinkType.STREAMING)
                .map(externalLink -> new Network(externalLink.url(), externalLink.icon(), externalLink.color()))
                .collect(Collectors.toList()) : null;
    }

    @Named("fromTmdbStatus")
    default VideoContentStatus fromTmdbStatus(String status){
        return switch (status.toLowerCase()){
            case "rumored" -> VideoContentStatus.RUMORED;
            case "planned", "in production", "post production" -> VideoContentStatus.NOT_YET_RELEASED;
            case "released", "ended" -> VideoContentStatus.FINISHED;
            case "canceled" -> VideoContentStatus.CANCELED;
            case "returning series", "premiered" -> VideoContentStatus.ONGOING;
            default -> throw new IllegalStateException("Unexpected value: " + status.toLowerCase());
        };
    }

    @Named("fromTmdbGenres")
    default List<String> fromTmdbGenres(List<Genre> genres){
        return genres.stream()
                .map(NamedIdElement::getName)
                .collect(Collectors.toList());
    }

    @Named("averageScore")
    default Float averageScore(Integer averageScore){
        return averageScore != null ? averageScore/10f : 0;
    }

    default List<String> fromTmdbSynonyms(List<AlternativeTitle> alternativeTitles, List<ProductionCountry> productionCountries, String originalTitle){
        Set<String> countries = productionCountries.stream()
                .map(ProductionCountry::getIsoCode)
                .collect(Collectors.toSet());
        countries.add("UA");
        Set<String> synonyms = alternativeTitles
                .stream()
                .filter(alternativeTitle ->  countries.contains(alternativeTitle.getIso31661()))
                .limit(10)
                .map(AlternativeTitle::getTitle)
                .collect(Collectors.toSet());
        synonyms.add(originalTitle);
        return synonyms.stream().toList();
    }

    @Named("fromTmdbProductionCompanies")
    default List<Studio> fromTmdbProductionCompanies(List<ProductionCompany> productionCompanies){
        return productionCompanies.stream()
                .map(productionCompany -> new Studio(productionCompany.getName()))
                .collect(Collectors.toList());
    }

    @Named("tvSeriesDuration")
    default Integer tvSeriesDuration(List<Integer> episodeRunTime){
        if(episodeRunTime.size() == 1){
            return episodeRunTime.getFirst();
        }
        OptionalDouble optionalRuntime = episodeRunTime.stream()
                .mapToInt(Integer::intValue)
                .average();
        return optionalRuntime.isPresent() ? (int) optionalRuntime.getAsDouble() : 0;
    }

    @Named("fromTmdbDate")
    default LocalDate fromTmdbDate(String date){
        return LocalDate.parse(date);
    }
}
