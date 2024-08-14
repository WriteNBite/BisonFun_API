package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;

public record VideoContentCompilation(
        VideoContent videoContentDb,
        AniListMedia aniListMedia,
        MovieDb movieDb,
        TvSeriesDb tvSeriesDb
) {
}
