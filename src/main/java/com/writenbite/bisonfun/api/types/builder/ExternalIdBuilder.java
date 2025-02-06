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
        if(externalId != null) {
            this.aniListId = externalId.aniListId();
            this.tmdbId = externalId.tmdbId();
            this.malId = externalId.malId();
            this.imdbId = externalId.imdbId();
        }
    }

    public ExternalIdBuilder aniListId(Integer aniListId) {
        if(isInvalidNumericId(this.aniListId) && !isInvalidNumericId(aniListId)){
            this.aniListId = aniListId;
        }
        return this;
    }

    public ExternalIdBuilder tmdbId(Integer tmdbId) {
        if(isInvalidNumericId(this.tmdbId) && !isInvalidNumericId(tmdbId)){
            this.tmdbId = tmdbId;
        }
        return this;
    }

    public ExternalIdBuilder malId(Integer malId) {
        if (isInvalidNumericId(this.malId) && !isInvalidNumericId(malId)) {
            this.malId = malId;
        }
        return this;
    }

    public ExternalIdBuilder imdbId(String imdbId) {
        if(isEmpty(this.imdbId) && !isEmpty(imdbId)) {
            this.imdbId = imdbId;
        }
        return this;
    }

    public ExternalIdBuilder updateFrom(ExternalId other){
        if(other != null){
            aniListId(other.aniListId());
            tmdbId(other.tmdbId());
            malId(other.malId());
            imdbId(other.imdbId());
        }
        return this;
    }

    public ExternalId build() {
        validate();
        return new ExternalId(aniListId, tmdbId, malId, imdbId);
    }

    private boolean isInvalidNumericId(Integer id){
        return id == null || id <= 0;
    }

    private boolean isEmpty(String value){
        return value == null || value.trim().isEmpty();
    }

    private boolean hasAnyId(){
        return !isInvalidNumericId(aniListId)
                || !isInvalidNumericId(tmdbId)
                || !isInvalidNumericId(malId)
                || !isEmpty(imdbId);
    }

    private void validate(){
        if(!hasAnyId()){
            throw new IllegalStateException("At least one valid external ID is required");
        }
    }
}
