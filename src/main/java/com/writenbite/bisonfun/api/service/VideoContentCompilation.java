package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;

public record VideoContentCompilation(
        VideoContent videoContentDb,
        AniListMedia aniListMedia,
        TmdbVideoContent tmdbVideoContent
) {
}
