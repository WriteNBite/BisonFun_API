package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.tv.core.Network;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TmdbTvSeriesVideoContent extends TmdbVideoContent{

    private final TvSeriesDb tv;

    public TmdbTvSeriesVideoContent(TvSeriesDb tv) {
        this.tv = tv;
    }

    @Override
    public Integer getTmdbId() {
        return tv.getId();
    }

    @Override
    public Boolean isAdult() {
        return tv.getAdult();
    }

    @Override
    public String getBackdropPath() {
        return tv.getBackdropPath();
    }

    @Override
    public List<Integer> getGenreIds() {
        return getGenres().stream().map(Genre::getId).collect(Collectors.toList());
    }

    @Override
    public List<Genre> getGenres() {
        return tv.getGenres();
    }

    @Override
    public Optional<String> getImdbId() {
        if(tv.getExternalIds() == null){
            return Optional.empty();
        }
        return Optional.ofNullable(tv.getExternalIds().getImdbId());
    }

    @Override
    public String getOriginalLanguage() {
        return tv.getOriginalLanguage();
    }

    @Override
    public String getOriginalTitle() {
        return tv.getOriginalName();
    }

    @Override
    public String getOverview() {
        return tv.getOverview();
    }

    @Override
    public Double getPopularity() {
        return tv.getPopularity();
    }

    @Override
    public String getPosterPath() {
        return tv.getPosterPath();
    }

    @Override
    public List<ProductionCompany> getProductionCompanies() {
        return tv.getProductionCompanies();
    }

    @Override
    public List<ProductionCountry> getProductionCountries() {
        return tv.getProductionCountries();
    }

    @Override
    public String getReleaseDate() {
        return tv.getFirstAirDate();
    }

    @Override
    public Optional<Integer> getRuntime() {
        List<Integer> episodeRuntime = tv.getEpisodeRunTime();
        if(episodeRuntime.isEmpty()){
            return Optional.empty();
        }
        episodeRuntime.sort(Comparator.naturalOrder());
        return Optional.ofNullable(episodeRuntime.get((episodeRuntime.size() - 1)/2));
    }

    @Override
    public List<Language> getSpokenLanguages() {
        return tv.getSpokenLanguages();
    }

    @Override
    public Status getStatus() {
        return switch (tv.getStatus()){
            case "Returning Series" -> Status.ONGOING;
            case "Planned" -> Status.PLANNED;
            case "In Production" -> Status.UPCOMING;
            case "Ended" -> Status.RELEASED;
            case "Canceled" -> Status.CANCELED;
            default -> Status.UNKNOWN;
        };
    }

    @Override
    public String getTagline() {
        return tv.getTagline();
    }

    @Override
    public String getTitle() {
        return tv.getName();
    }

    @Override
    public Double getVoteAverage() {
        return tv.getVoteAverage();
    }

    @Override
    public List<AlternativeTitle> getAlternativeTitles() {
        return tv.getAlternativeTitles().getResults();
    }

    @Override
    public List<Keyword> getKeywords() {
        return tv.getKeywords().getResults();
    }

    @Override
    public List<TmdbSimpleVideoContent> getRecommendations() {
        if(tv.getRecommendations() == null){
            return Collections.emptyList();
        }
        return tv.getRecommendations()
                .getResults()
                .stream()
                .map(TmdbSimpleTvSeriesVideoContent::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getLastEpisodeToAir() {
        return tv.getLastEpisodeToAir().getAirDate();
    }

    @Override
    public String getStringStatus() {
        return tv.getStatus();
    }

    @Override
    public Integer getSeasons() {
        return tv.getSeasons().size();
    }

    @Override
    public List<Network> getNetworks() {
        return tv.getNetworks();
    }

    @Override
    public int getEpisodes() {
        return tv.getNumberOfEpisodes() != null ? tv.getNumberOfEpisodes() : 0;
    }

    @Override
    public VideoContentType getVideoContentType() {
        return VideoContentType.TV;
    }
}
