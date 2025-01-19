package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import info.movito.themoviedbapi.model.core.TvSeries;

import java.util.List;

public class TmdbSimpleTvSeriesVideoContent extends TmdbSimpleVideoContent{

    private final TvSeries tvSeries;

    public TmdbSimpleTvSeriesVideoContent(TvSeries tvSeries) {
        this.tvSeries = tvSeries;
    }

    @Override
    public Integer getTmdbId() {
        return tvSeries.getId();
    }

    @Override
    public Boolean isAdult() {
        return tvSeries.getAdult();
    }

    @Override
    public String getBackdropPath() {
        return tvSeries.getBackdropPath();
    }

    @Override
    public List<Integer> getGenreIds() {
        return tvSeries.getGenreIds();
    }

    @Override
    public String getOriginalLanguage() {
        return tvSeries.getOriginalLanguage();
    }

    @Override
    public String getOriginalTitle() {
        return tvSeries.getOriginalName();
    }

    @Override
    public String getOverview() {
        return tvSeries.getOverview();
    }

    @Override
    public String getTitle() {
        return tvSeries.getName();
    }

    @Override
    public Double getPopularity() {
        return tvSeries.getPopularity();
    }

    @Override
    public String getPosterPath() {
        return tvSeries.getPosterPath();
    }

    @Override
    public String getReleaseDate() {
        return tvSeries.getFirstAirDate();
    }

    @Override
    public Double getVoteAverage() {
        return tvSeries.getVoteAverage();
    }

    @Override
    public VideoContentType getVideoContentType() {
        return VideoContentType.TV;
    }
}
