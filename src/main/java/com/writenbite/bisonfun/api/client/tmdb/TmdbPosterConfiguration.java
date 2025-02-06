package com.writenbite.bisonfun.api.client.tmdb;

import lombok.Getter;

@Getter
public enum TmdbPosterConfiguration {
    TINY("w92"),
    MINI("w154"),
    SMALL("w185"),
    MEDIUM("w342"),
    DEFAULT("w500"),
    LARGE("w780"),
    ORIGINAL("original");

    private static final String base_url = "https://image.tmdb.org/t/p/";


    private final String url;

    TmdbPosterConfiguration(String size) {
        url = base_url+size;
    }
}