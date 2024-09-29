package com.writenbite.bisonfun.api.config;

import info.movito.themoviedbapi.model.core.*;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Model;
import org.instancio.generator.specs.StringGeneratorSpec;
import org.instancio.generators.Generators;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.instancio.Select.*;

@Configuration
public class ModelConfig {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Model<MovieDb> getMovieDbModel(){
        return ofTmdbApi(MovieDb.class)
                .set(field(AbstractJsonMapping::getNewItems), new HashMap<>())
                .set(field(MovieDb::getAdult), false)
                .generate(field(MovieDb::getBackdropPath), this::imageUrlGeneratorSpec)
                .set(field(MovieDb::getBelongsToCollection), null)
                .generate(field(MovieDb::getHomepage), gen -> gen.string()
                        .prefix("https://www.")
                        .suffix(".com"))
                .generate(field(MovieDb::getImdbID), gen -> gen.string()
                        .prefix("tt")
                        .digits()
                        .length(7))
                .set(field(MovieDb::getOriginalLanguage), "en")
                .generate(field(MovieDb::getOverview), gen -> gen.text().loremIpsum())
                .generate(field(MovieDb::getPopularity), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .generate(field(MovieDb::getPosterPath), this::imageUrlGeneratorSpec)
                .set(field(MovieDb::getProductionCompanies), Instancio.ofList(getProductionCompanyModel()).size(2).create())
                .set(field(MovieDb::getProductionCountries), Instancio.ofList(getProductionCountryModel()).size(2).create())
                .generate(field(MovieDb::getReleaseDate), gen -> gen.temporal()
                        .localDate()
                        .past()
                        .as(date -> date.format(formatter)))
                .set(field(MovieDb::getSpokenLanguages), Instancio.ofList(getLanguage()).size(2).create())
                .set(field(MovieDb::getStatus), "Released")
                .set(field(MovieDb::getTagline), "This is the tagline.")
                .set(field(MovieDb::getVideo), false)
                .generate(field(MovieDb::getVoteAverage), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .set(field(MovieDb::getRecommendations), Instancio.of(getMovieResultsPage()).create())
                .set(field(MovieDb::getAccountStates), null)
                .set(field(MovieDb::getCredits), null)
                .set(field(MovieDb::getChanges), null)
                .set(field(MovieDb::getExternalIds), null)
                .set(field(MovieDb::getImages), null)
                .set(field(MovieDb::getReleaseDates), null)
                .set(field(MovieDb::getLists), null)
                .set(field(MovieDb::getReviews), null)
                .set(field(MovieDb::getSimilar), null)
                .set(field(MovieDb::getTranslations), null)
                .set(field(MovieDb::getVideos), null)
                .set(field(MovieDb::getWatchProviders), null)
                .toModel();
    }

    @Bean
    public Model<TvSeriesDb> getTvSeriesDbModel(){
        return ofTmdbApi(TvSeriesDb.class)
                .set(field(TvSeriesDb::getAdult), false)
                .generate(field(TvSeriesDb::getBackdropPath), this::imageUrlGeneratorSpec)
                .generate(field(TvSeriesDb::getFirstAirDate), gen -> gen.temporal()
                        .localDate()
                        .past()
                        .as(date -> date.format(formatter)))
                .generate(field(TvSeriesDb::getLastAirDate), gen -> gen.temporal()
                        .localDate()
                        .past()
                        .as(date -> date.format(formatter)))
                .generate(field(TvSeriesDb::getHomepage), gen -> gen.string()
                        .prefix("https://www.")
                        .suffix(".com"))
                .set(field(TvSeriesDb::getOriginalLanguage), "en")
                .generate(field(TvSeriesDb::getOverview), gen -> gen.text().loremIpsum())
                .generate(field(TvSeriesDb::getPopularity), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .generate(field(TvSeriesDb::getPosterPath), this::imageUrlGeneratorSpec)
                .set(field(TvSeriesDb::getProductionCompanies), Instancio.ofList(getProductionCompanyModel()).size(2).create())
                .set(field(TvSeriesDb::getProductionCountries), Instancio.ofList(getProductionCountryModel()).size(2).create())
                .set(field(TvSeriesDb::getSpokenLanguages), Instancio.ofList(getLanguage()).size(2).create())
                .set(field(TvSeriesDb::getStatus), "Released")
                .set(field(TvSeriesDb::getTagline), "This is the tagline.")
                .generate(field(TvSeriesDb::getVoteAverage), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .set(field(TvSeriesDb::getRecommendations), Instancio.of(getTvSeriesResultsPage()).create())
                .set(field(TvSeriesDb::getAccountStates), null)
                .set(field(TvSeriesDb::getCredits), null)
                .set(field(TvSeriesDb::getChanges), null)
                .set(field(TvSeriesDb::getContentRatings), null)
                .set(field(TvSeriesDb::getEpisodeGroups), null)
                .set(field(TvSeriesDb::getImages), null)
                .set(field(TvSeriesDb::getLists), null)
                .set(field(TvSeriesDb::getReviews), null)
                .set(field(TvSeriesDb::getScreenedTheatrically), null)
                .set(field(TvSeriesDb::getSimilar), null)
                .set(field(TvSeriesDb::getTranslations), null)
                .set(field(TvSeriesDb::getVideos), null)
                .set(field(TvSeriesDb::getWatchProviders), null)
                .toModel();
    }

    @Bean
    public Model<ProductionCompany> getProductionCompanyModel(){
        return ofTmdbApi(ProductionCompany.class)
                .generate(field(ProductionCompany::getLogoPath), this::imageUrlGeneratorSpec)
                .generate(field(ProductionCompany::getOriginCountry), gen -> gen.string()
                        .length(2)
                        .upperCase())
                .toModel();
    }

    @Bean
    public Model<ProductionCountry> getProductionCountryModel(){
        return ofTmdbApi(ProductionCountry.class)
                .generate(field(ProductionCountry::getIsoCode), gen -> gen.string()
                        .length(2)
                        .upperCase())
                .toModel();
    }

    @Bean
    public Model<Language> getLanguage(){
        return ofTmdbApi(Language.class)
                .generate(field(Language::getIso6391), gen -> gen.string()
                        .lowerCase()
                        .length(2))
                .toModel();
    }

    @Bean
    public Model<MovieResultsPage> getMovieResultsPage(){
        return ofTmdbApi(MovieResultsPage.class)
                .set(field(MovieResultsPage::getPage), 1)
                .set(field(MovieResultsPage::getTotalPages), 1)
                .set(field(MovieResultsPage::getTotalResults), 20)
                .set(field(MovieResultsPage::getResults), Instancio.ofList(getMovie()).size(20).create())
                .toModel();
    }

    @Bean
    public Model<TvSeriesResultsPage> getTvSeriesResultsPage(){
        return ofTmdbApi(TvSeriesResultsPage.class)
                .set(field(TvSeriesResultsPage::getPage), 1)
                .set(field(TvSeriesResultsPage::getTotalPages), 1)
                .set(field(TvSeriesResultsPage::getTotalResults), 20)
                .set(field(TvSeriesResultsPage::getResults), Instancio.ofList(getTvSeries()).size(20).create())
                .toModel();
    }

    @Bean
    public Model<Movie> getMovie(){
        return  ofTmdbApi(Movie.class)
                .set(field(Movie::getAdult), false)
                .generate(field(Movie::getBackdropPath), this::imageUrlGeneratorSpec)
                .set(field(Movie::getOriginalLanguage), "en")
                .generate(field(Movie::getOverview), gen -> gen.text().loremIpsum())
                .generate(field(Movie::getPopularity), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .generate(field(Movie::getPosterPath), this::imageUrlGeneratorSpec)
                .generate(field(Movie::getReleaseDate), gen -> gen.temporal()
                        .localDate()
                        .past()
                        .as(date -> date.format(formatter)))
                .set(field(Movie::getVideo), false)
                .generate(field(Movie::getVoteAverage), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .toModel();
    }

    @Bean
    public Model<TvSeries> getTvSeries(){
        return ofTmdbApi(TvSeries.class)
                .set(field(TvSeries::getAdult), false)
                .generate(field(TvSeries::getBackdropPath), this::imageUrlGeneratorSpec)
                .set(field(TvSeries::getOriginalLanguage), "en")
                .generate(field(TvSeries::getOverview), gen -> gen.text().loremIpsum())
                .generate(field(TvSeries::getPopularity), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .generate(field(TvSeries::getPosterPath), this::imageUrlGeneratorSpec)
                .generate(field(TvSeries::getFirstAirDate), gen -> gen.temporal()
                        .localDate()
                        .past()
                        .as(date -> date.format(formatter)))
                .generate(field(TvSeries::getVoteAverage), gen -> gen.doubles()
                        .range(0.0, 10.0))
                .toModel();
    }

    public static <T extends AbstractJsonMapping> InstancioApi<T> ofTmdbApi(Class<T> tClass){
        return Instancio.of(tClass)
                .lenient()
                .set(fields(field -> field.getName().contains("newItems")), new HashMap<>());
//                .set(field(T::getNewItems), new HashMap<>());
    }

    private StringGeneratorSpec imageUrlGeneratorSpec(Generators gen){
        return gen.string()
                .prefix("/")
                .length(12)
                .suffix(".jpg");
    }
}
