package com.writenbite.bisonfun.api.client.anilist.types;

import java.io.Serializable;
import java.util.List;

public interface AniListPage<T> extends Serializable {

    AniListPageInfo getPageInfo();

    List<T> getList();
}
