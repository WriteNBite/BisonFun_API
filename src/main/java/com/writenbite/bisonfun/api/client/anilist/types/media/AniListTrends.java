package com.writenbite.bisonfun.api.client.anilist.types.media;

import com.google.gson.annotations.SerializedName;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

import java.io.Serializable;
import java.util.List;

public record AniListTrends(
        @SerializedName("tv_trends") AniListMediaPage tvTrends,
        @SerializedName("movie_trends") AniListMediaPage movieTrends,
        @SerializedName("music_trends") AniListMediaPage musicTrends,
        @SerializedName("special_trends") AniListMediaPage specialTrends
) implements Serializable {
    public List<AniListMedia> getTrendsByFormat(VideoContentFormat format) {
        return switch (format){
            case MOVIE -> movieTrends.getList();
            case TV -> tvTrends.getList();
            case SPECIAL -> specialTrends.getList();
            case MUSIC -> musicTrends.getList();
            default -> List.of();
        };
    }
}
