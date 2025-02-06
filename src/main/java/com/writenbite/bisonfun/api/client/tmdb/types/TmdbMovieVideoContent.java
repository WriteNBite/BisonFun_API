package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.core.Network;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TmdbMovieVideoContent extends TmdbVideoContent {

    private final MovieDb movie;

    public TmdbMovieVideoContent(MovieDb movieDb) {
        this.movie = movieDb;
    }

    @Override
    public Integer getTmdbId() {
        return movie.getId();
    }

    @Override
    public Boolean isAdult() {
        return movie.getAdult();
    }

    @Override
    public String getBackdropPath() {
        return movie.getBackdropPath();
    }

    @Override
    public List<Integer> getGenreIds() {
        return getGenres().stream().map(Genre::getId).collect(Collectors.toList());
    }

    @Override
    public List<Genre> getGenres() {
        return movie.getGenres();
    }

    @Override
    public Optional<String> getImdbId() {
        return Optional.ofNullable(movie.getImdbID());
    }

    @Override
    public String getOriginalLanguage() {
        return movie.getOriginalLanguage();
    }

    @Override
    public String getOriginalTitle() {
        return movie.getOriginalTitle();
    }

    @Override
    public String getOverview() {
        return movie.getOverview();
    }

    @Override
    public Double getPopularity() {
        return movie.getPopularity();
    }

    @Override
    public String getPosterPath() {
        return movie.getPosterPath();
    }

    @Override
    public List<ProductionCompany> getProductionCompanies() {
        return movie.getProductionCompanies();
    }

    @Override
    public List<ProductionCountry> getProductionCountries() {
        return movie.getProductionCountries();
    }

    @Override
    public String getReleaseDate() {
        return movie.getReleaseDate();
    }

    @Override
    public Optional<Integer> getRuntime() {
        return Optional.ofNullable(movie.getRuntime());
    }

    @Override
    public List<Language> getSpokenLanguages() {
        return movie.getSpokenLanguages();
    }

    @Override
    public Status getStatus() {
        return switch (movie.getStatus()){
            case "Rumored" -> Status.RUMORED;
            case "Planned" -> Status.PLANNED;
            case "In Production", "Post Production" -> Status.UPCOMING;
            case "Released" -> Status.RELEASED;
            case "Canceled" -> Status.CANCELED;
            default -> Status.UNKNOWN;
        };
    }

    @Override
    public String getTagline() {
        return movie.getTagline();
    }

    @Override
    public String getTitle() {
        return movie.getTitle();
    }

    @Override
    public Double getVoteAverage() {
        return movie.getVoteAverage();
    }

    @Override
    public List<AlternativeTitle> getAlternativeTitles() {
        return movie.getAlternativeTitles().getTitles();
    }

    @Override
    public List<Keyword> getKeywords() {
        return movie.getKeywords().getKeywords();
    }

    @Override
    public List<TmdbSimpleVideoContent> getRecommendations() {
        if(movie.getRecommendations() == null){
            return Collections.emptyList();
        }
        return movie.getRecommendations()
                .getResults()
                .stream()
                .map(TmdbSimpleMovieVideoContent::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getLastEpisodeToAir() {
        return movie.getReleaseDate();
    }

    @Override
    public String getStringStatus() {
        return movie.getStatus();
    }

    @Override
    public Integer getSeasons() {
        return 1;
    }

    @Override
    public List<Network> getNetworks() {
        return List.of();
    }

    @Override
    public int getEpisodes() {
        if(getStatus() == Status.RELEASED){
            return 1;
        }
        return 0;
    }

    @Override
    public VideoContentType getVideoContentType() {
        return VideoContentType.MOVIE;
    }
}
