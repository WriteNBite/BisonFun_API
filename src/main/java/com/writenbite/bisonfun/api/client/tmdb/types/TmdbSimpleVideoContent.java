package com.writenbite.bisonfun.api.client.tmdb.types;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.service.RawVideoContent;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public abstract class TmdbSimpleVideoContent implements RawVideoContent, Serializable {
    public abstract Integer getTmdbId();
    public abstract Boolean isAdult();
    public abstract String getBackdropPath();
    public abstract List<Integer> getGenreIds();
    public abstract String getOriginalLanguage();
    public abstract String getOriginalTitle();
    public abstract String getOverview();
    public abstract String getTitle();
    public abstract Double getPopularity();
    public abstract String getPosterPath();
    public abstract String getReleaseDate();
    public abstract Double getVoteAverage();
    public abstract VideoContentType getVideoContentType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TmdbSimpleVideoContent that)) return false;

        return Objects.equals(getTmdbId(), that.getTmdbId()) &&
                Objects.equals(isAdult(), that.isAdult()) &&
                Objects.equals(getBackdropPath(), that.getBackdropPath()) &&
                Objects.equals(getGenreIds(), that.getGenreIds()) &&
                Objects.equals(getOriginalLanguage(), that.getOriginalLanguage()) &&
                Objects.equals(getOriginalTitle(), that.getOriginalTitle()) &&
                Objects.equals(getOverview(), that.getOverview()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getPopularity(), that.getPopularity()) &&
                Objects.equals(getPosterPath(), that.getPosterPath()) &&
                Objects.equals(getReleaseDate(), that.getReleaseDate()) &&
                Objects.equals(getVoteAverage(), that.getVoteAverage()) &&
                Objects.equals(getVideoContentType(), that.getVideoContentType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getTmdbId(),
                isAdult(),
                getBackdropPath(),
                getGenreIds(),
                getOriginalLanguage(),
                getOriginalTitle(),
                getOverview(),
                getTitle(),
                getPopularity(),
                getPosterPath(),
                getReleaseDate(),
                getVoteAverage(),
                getVideoContentType()
        );
    }
}
