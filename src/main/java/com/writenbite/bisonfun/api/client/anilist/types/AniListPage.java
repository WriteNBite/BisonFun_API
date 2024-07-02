package com.writenbite.bisonfun.api.client.anilist.types;

import java.util.List;

public interface AniListPage<T> {

    AniListPageInfo getPageInfo();

    List<T> getList();
}
