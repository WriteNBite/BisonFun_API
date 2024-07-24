package com.writenbite.bisonfun.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.writenbite.bisonfun.api.client.tmdb.TmdbApiResponse;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbContentType;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import kong.unirest.core.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TmdbClientTest {

    @Mock
    private TmdbApiResponse parser;

    private String oppenheimer;
    private String turkeyMovieList;
    private String demonSlayer;

    @BeforeEach
    public void setUp() throws IOException {
        Resource oppenheimerResource = new ClassPathResource("oppenheimer.json");
        oppenheimer = new String(Files.readAllBytes(Paths.get(oppenheimerResource.getURI())));
        Resource turkeyMovieListResource = new ClassPathResource("turkey_movies.json");
        turkeyMovieList = new String(Files.readAllBytes(Paths.get(turkeyMovieListResource.getURI())));
        Resource demonSlayerResource = new ClassPathResource("demon_slayer.json");
        demonSlayer = new String(Files.readAllBytes(Paths.get(demonSlayerResource.getURI())));
    }

    @Test
    public void testParseMovieById() throws JsonProcessingException {
        TmdbClient tmdbClient = new TmdbClient(parser, new ObjectMapper());

        when(parser.getMovieById(872585)).thenReturn(new JSONObject(oppenheimer));

        MovieDb movie =  tmdbClient.parseMovieById(872585);

        System.out.println(movie.toString());
        Assertions.assertEquals(false, movie.getAdult());
        Assertions.assertEquals(872585, movie.getId());
        Assertions.assertEquals("Oppenheimer", movie.getTitle());
        Assertions.assertEquals("The world forever changes.", movie.getTagline());
        Assertions.assertEquals("Released", movie.getStatus());
        Assertions.assertEquals(8.11, movie.getVoteAverage(), 0.5);
        Assertions.assertEquals(7677, (long) movie.getVoteCount());
        Assertions.assertEquals("US", movie.getAlternativeTitles().getTitles().get(1).getIso31661());
        Assertions.assertEquals("Gadget", movie.getAlternativeTitles().getTitles().get(1).getTitle());
        Assertions.assertEquals("working title", movie.getAlternativeTitles().getTitles().get(1).getType());
        Assertions.assertEquals("tt15398776", movie.getImdbID());
        Assertions.assertEquals("The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.", movie.getOverview());
        Assertions.assertEquals(2, movie.getGenres().size());
        Assertions.assertEquals("Drama", movie.getGenres().get(0).getName());
        Assertions.assertEquals("History", movie.getGenres().get(1).getName());
    }

    @Test
    public void testParseMovieList() throws JsonProcessingException{
        TmdbClient tmdbClient = new TmdbClient(parser, new ObjectMapper());

        when(parser.getTMDBList("turkey", TmdbContentType.MOVIE, 1)).thenReturn(new JSONObject(turkeyMovieList));

        MovieResultsPage result = tmdbClient.parseMovieList("turkey", 1);

        System.out.println(result.toString());
        //Testing result info
        Assertions.assertEquals(1, result.getPage());
        Assertions.assertEquals(117, result.getTotalResults());
        Assertions.assertEquals(6, result.getTotalPages());
        //Testing entity info
        Movie movie = result.getResults().getFirst();
        Assertions.assertEquals(false, movie.getAdult());
        Assertions.assertNull(movie.getBackdropPath());
        Assertions.assertEquals(3, movie.getGenreIds().size());
        Assertions.assertEquals(924036, movie.getId());
        Assertions.assertEquals("en", movie.getOriginalLanguage());
        Assertions.assertEquals("TURKEY", movie.getOriginalTitle());
        Assertions.assertEquals("A young man's unrelenting quest to achieve three consecutive bowling strikes.", movie.getOverview());
        Assertions.assertEquals(0.6, movie.getPopularity());
        Assertions.assertEquals("/rD2OMoS951Fid2kJvJ6pqXUBlwl.jpg", movie.getPosterPath());
        Assertions.assertEquals("2021-12-14", movie.getReleaseDate());
        Assertions.assertEquals("TURKEY", movie.getTitle());
        Assertions.assertEquals(false, movie.getVideo());
        Assertions.assertEquals(0, movie.getVoteAverage());
        Assertions.assertEquals(0, movie.getVoteCount());
    }

    @Test
    public void testParseTvById() throws JsonProcessingException{
        TmdbClient tmdbClient = new TmdbClient(parser, new ObjectMapper());
        when(parser.getShowById(85937)).thenReturn(new JSONObject(demonSlayer));

        TvSeriesDb tv = tmdbClient.parseShowById(85937);
        System.out.println(tv.toString());

        Assertions.assertEquals(85937, tv.getId());
        Assertions.assertEquals(3, tv.getGenres().size());
        Assertions.assertEquals(8.5, tv.getVoteAverage(), 0.5);
        Assertions.assertEquals(false, tv.getAdult());
        Assertions.assertEquals("Demon Slayer: Kimetsu no Yaiba", tv.getName());
        Assertions.assertEquals("", tv.getTagline());
        Assertions.assertEquals("Returning Series", tv.getStatus());
        Assertions.assertEquals(8.7, tv.getVoteAverage(), 0.5);
        Assertions.assertEquals(5972, (long) tv.getVoteCount());
        Assertions.assertEquals("tt9335498", tv.getExternalIds().getImdbId());
        Assertions.assertEquals("It is the Taisho Period in Japan. Tanjiro, a kindhearted boy who sells charcoal for a living, finds his family slaughtered by a demon. To make matters worse, his younger sister Nezuko, the sole survivor, has been transformed into a demon herself. Though devastated by this grim reality, Tanjiro resolves to become a “demon slayer” so that he can turn his sister back into a human, and kill the demon that massacred his family.", tv.getOverview());
        Assertions.assertEquals(3, tv.getGenres().size());
        Assertions.assertEquals("Animation", tv.getGenres().getFirst().getName());
        Assertions.assertEquals("Action & Adventure", tv.getGenres().get(1).getName());
    }
}
