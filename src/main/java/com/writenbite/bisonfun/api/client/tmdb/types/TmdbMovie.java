package com.writenbite.bisonfun.api.client.tmdb.types;

import info.movito.themoviedbapi.model.core.ProductionCompany;

import java.util.List;

public record TmdbMovie(
        Integer id,
        Integer imdbId,
        Boolean adult,
        String backdropPath,
        String belongsToCollection,
        Integer budget,
        List<String> genres,
        String homePage,
        String originalLanguage,
        String originalTitle,
        String overview,
        Float popularity,
        String posterPath,
        List<ProductionCompany> productionCompanies
) {
}
