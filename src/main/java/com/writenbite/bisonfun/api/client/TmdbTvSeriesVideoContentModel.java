package com.writenbite.bisonfun.api.client;

import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;

public class TmdbTvSeriesVideoContentModel implements VideoContentModel{
    private final TvSeriesDb tv;

    public TmdbTvSeriesVideoContentModel(TvSeriesDb tv) {
        this.tv = tv;
    }

    @Override
    public int getEpisodes() {
        return tv.getNumberOfEpisodes();
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
}
