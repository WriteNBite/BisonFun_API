package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.client.VideoContentModel;
import com.writenbite.bisonfun.api.service.RawVideoContent;
import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.tv.core.Network;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public abstract class TmdbVideoContent extends TmdbSimpleVideoContent implements VideoContentModel, RawVideoContent {
    public abstract List<Genre> getGenres();
    public abstract Optional<String> getImdbId();
    public abstract List<ProductionCompany> getProductionCompanies();
    public abstract List<ProductionCountry> getProductionCountries();
    public abstract Optional<Integer> getRuntime();
    public abstract List<Language> getSpokenLanguages();
    public abstract String getTagline();
    public abstract List<AlternativeTitle> getAlternativeTitles();
    public abstract List<Keyword> getKeywords();
    public abstract List<TmdbSimpleVideoContent> getRecommendations();
    public abstract String getLastEpisodeToAir();
    public abstract String getStringStatus();
    public abstract Integer getSeasons();
    public abstract List<Network> getNetworks();
}
