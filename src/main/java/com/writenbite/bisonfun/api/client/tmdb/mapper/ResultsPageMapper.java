package com.writenbite.bisonfun.api.client.tmdb.mapper;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContentResultsPage;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.ResultsPage;
import info.movito.themoviedbapi.model.core.TvSeries;
import org.mapstruct.Mapper;

@Mapper(uses = {MovieMapper.class, TvSeriesMapper.class})
public interface ResultsPageMapper {
    TmdbVideoContentResultsPage fromMovieResultsPage(ResultsPage<Movie> resultsPage);
    TmdbVideoContentResultsPage fromTvSeriesResultsPage(ResultsPage<TvSeries> resultsPage);
}
