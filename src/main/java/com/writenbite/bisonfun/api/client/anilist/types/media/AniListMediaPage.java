package com.writenbite.bisonfun.api.client.anilist.types.media;

import com.google.gson.annotations.SerializedName;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPageInfo;
import lombok.ToString;

import java.util.List;

@ToString
public class AniListMediaPage implements AniListPage<AniListMedia> {
    @SerializedName("media")
    List<AniListMedia> list;
    final AniListPageInfo pageInfo;

    public AniListMediaPage(AniListPageInfo pageInfo, List<AniListMedia> list) {
        this.list = list;
        this.pageInfo = pageInfo;
    }

    @Override
    public AniListPageInfo getPageInfo() {
        return pageInfo;
    }

    @Override
    public List<AniListMedia> getList() {
        return list;
    }
}
