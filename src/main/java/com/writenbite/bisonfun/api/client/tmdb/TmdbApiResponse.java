package com.writenbite.bisonfun.api.client.tmdb;

import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbContentType;
import kong.unirest.core.GetRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class TmdbApiResponse {

    @Value("#{environment['bisonfun.tmdb.key']}")
    private String tmdbKey;

    /**
     * Get movie by id. Cacheable as "jsonMovie".
     * @param id identification number from TheMovieDB database.
     * @return JSONObject with info about movie (id, title, description, etc.).
     */
    @Cacheable("jsonMovie")
    public JSONObject getMovieById(int id){
        log.info("Get Movie: {}", id);
        //Get JSON of movie by id from TMDB
        HttpResponse<String> result = Unirest.get(TMDB.MOVIE.link)
                .routeParam("movie_id", Integer.toString(id))
                .queryString("api_key", tmdbKey)
                .queryString("language", "en-US")
                .queryString("append_to_response", "keywords,recommendations,alternative_titles")
                .asString();

        log.debug(result.getBody());

        if(result.getStatus() == 401){
            log.error("Invalid API key (TMDB API key)");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }else if(result.getStatus() == 404){
            log.error("Movie couldn't be found: "+id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return new JSONObject(result.getBody());
    }
    /**
     * Get tv by id. Cacheable as "jsonShow".
     * @param id identification number from TheMovieDB database.
     * @return JSONObject with info about tv (id, title, description, etc.).
     */
    @Cacheable("jsonShow")
    public JSONObject getShowById(int id){
        log.info("Get TV: {}", id);
        // get JSON of tv-show by id from TMDB
        HttpResponse<String> result = Unirest.get(TMDB.TV.link)
                .routeParam("tv_id", Integer.toString(id))
                .queryString("api_key", tmdbKey)
                .queryString("language", "en-US")
                .queryString("append_to_response", "keywords,recommendations,external_ids,alternative_titles")
                .asString();

        log.debug(result.getBody());

        if(result.getStatus() == 401){
            log.error("Invalid API key (TMDB API key)");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }else if(result.getStatus() == 404){
            log.error("TV Show couldn't be found: "+id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return new JSONObject(result.getBody());
    }
    /**
     * Get movie\tv list by search query.
     * @param query string query to find movie\tv.
     * @param contentType (Movie or TV).
     * @param page current page of list.
     * @param year year of release
     * @return JSONObject with page info(current page, etc.) and JSONArray with movie\tv JSONObjects.
     */
    public JSONObject getTMDBList(String query, TmdbContentType contentType, int page, Integer year){
        log.info("Get list of {} by \"{}\" Page: {}", contentType, query, page);
        //get list of content from TMDB
        GetRequest request = Unirest.get(contentType == TmdbContentType.MOVIE ? TMDB.SEARCH_MOVIE.link : TMDB.SEARCH_TV.link)
                .queryString("api_key", tmdbKey)
                .queryString("language", "en-US")
                .queryString("query", query)
                .queryString("page", page)
                .queryString("include_adult", false);
        if (year != null){
            request.queryString("year", year);
        }
        HttpResponse<String> result = request.asString();

        log.debug(result.getBody());

        if(result.getStatus() == 404){
            log.error("Content couldn't be found: "+query);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }else if(result.getStatus() == 401){
            log.error("Invalid API key (TMDB API key)");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new JSONObject(result.getBody());
    }

    public JSONObject getTMDBList(String query, TmdbContentType contentType, int page){
        return getTMDBList(query, contentType, page, null);
    }
    /**
     * Get list of movie trends. Cacheable as "movieTrends".
     * @return JSONObject with page info(current page, etc.) and JSONArray with movie JSONObjects.
     * @throws NoAccessException if app can't access to TheMovieDB API.
     */
    @Cacheable("movieTrends")
    public JSONObject getMovieTrends() throws NoAccessException {
        log.info("Get movie trends");
        HttpResponse<String> result;
        try {
            result = Unirest.get(TMDB.TRENDS_MOVIE.link)
                    .queryString("api_key", tmdbKey)
                    .asString();
        }catch (Exception e){
            log.error("Caught exception {}", e.getMessage());
            throw new NoAccessException("Can't access to TheMovieDB");
        }
        if(result.getStatus() == 401){
            log.error("Invalid API key (TMDB API key)");
            throw new NoAccessException("Can't access to TheMovieDB (wrong API key)");
        }
        return new JSONObject(result.getBody());
    }
    /**
     * Get list of tv trends. Cacheable as "tvTrends".
     * @return JSONObject with page info(current page, etc.) and JSONArray with tv JSONObjects.
     * @throws NoAccessException if app can't access to TheMovieDB API.
     */
    @Cacheable("tvTrends")
    public JSONObject getTvTrends() throws NoAccessException {
        log.info("Get tv trends");
        HttpResponse<String> result;
        try {
            result = Unirest.get(TMDB.TRENDS_TV.link)
                    .queryString("api_key", tmdbKey)
                    .asString();
        }catch (Exception e){
            log.error("Caught exception {}", e.getMessage());
            throw new NoAccessException("Can't access to TheMovieDB");
        }
        if(result.getStatus() == 401){
            log.error("Invalid API key (TMDB API key)");
            throw new NoAccessException("Can't access to TheMovieDB (wrong API key)");
        }
        return new JSONObject(result.getBody());
    }
}
