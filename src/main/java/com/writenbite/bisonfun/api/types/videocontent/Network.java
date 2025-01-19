package com.writenbite.bisonfun.api.types.videocontent;

import com.writenbite.bisonfun.api.client.tmdb.TmdbPosterConfiguration;

public record Network(
        String url,
        String icon,
        String color
) {
    public String url() {
        if (url.startsWith("/")){
            return TmdbPosterConfiguration.ORIGINAL.getUrl() + url;
        }
        return url;
    }
}
