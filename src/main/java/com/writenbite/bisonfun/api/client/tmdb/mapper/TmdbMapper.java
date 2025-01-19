package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.TmdbAnimeChecker;
import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import com.writenbite.bisonfun.api.types.videocontent.Network;
import com.writenbite.bisonfun.api.types.videocontent.Studio;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentStatus;
import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.keywords.Keyword;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface TmdbMapper {
    @Named("tmdbYear")
    default int tmdbYear(String releaseDate){
        try{
            Date date = Date.valueOf(releaseDate);
            return date.toLocalDate().getYear();
        }catch (Exception e){
            return -1;
        }
    }

    @Named("toNetworkType")
    @Mapping(target = "url", source = "logoPath")
    Network toNetworkType(info.movito.themoviedbapi.model.tv.core.Network network);

    @IterableMapping(qualifiedByName = "toNetworkType")
    List<Network> toNetworkTypeList(List<info.movito.themoviedbapi.model.tv.core.Network> networks);

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

    default VideoContentCategory toEntityCategory(List<Keyword> keywords){
        if (!keywords.isEmpty()) {
            return TmdbAnimeChecker.animeKeywordCheck(keywords) ? VideoContentCategory.ANIME : VideoContentCategory.MAINSTREAM;
        }
        return VideoContentCategory.MAINSTREAM;
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
