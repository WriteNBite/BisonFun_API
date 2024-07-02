package com.writenbite.bisonfun.api.types.builder;

import com.writenbite.bisonfun.api.types.videocontent.ExternalId;

public class ExternalIdBuilder {
    private Integer aniListId;
    private Integer tmdbId;
    private Integer malId;
    private String imdbId;

    public ExternalIdBuilder() {
    }

    public ExternalIdBuilder(ExternalId externalId){
        this.aniListId = externalId.aniListId();
        this.tmdbId = externalId.tmdbId();
        this.malId = externalId.malId();
        this.imdbId = externalId.imdbId();
    }

    public ExternalIdBuilder aniListId(Integer aniListId) {
        this.aniListId = aniListId;
        return this;
    }

    public ExternalIdBuilder tmdbId(Integer tmdbId) {
        this.tmdbId = tmdbId;
        return this;
    }

    public ExternalIdBuilder malId(Integer malId) {
        this.malId = malId;
        return this;
    }

    public ExternalIdBuilder imdbId(String imdbId) {
        this.imdbId = imdbId;
        return this;
    }

    public ExternalId build() {
        return new ExternalId(aniListId, tmdbId, malId, imdbId);
    }
}
