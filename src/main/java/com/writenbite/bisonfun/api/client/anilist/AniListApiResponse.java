package com.writenbite.bisonfun.api.client.anilist;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@Component
@Slf4j
public class AniListApiResponse {

    /**
     * Get anime list by search query.
     * @param search string query to get list.
     * @param page current page of list
     * @return "Page" JSONObject which have page info(number, count, etc.) and JSONArray of anime "Media" JSONObjects.
     * @throws TooManyAnimeRequestsException if Anilist.co have got more requests than limit from app's account.
     * */
    public JSONObject getAnimeList(String search, int page, Collection<AniListMediaFormat> formats) throws TooManyAnimeRequestsException{
        log.info("Get list of Anime by \"{}\" Page: {}", search, page);

        JSONArray jsonFormats = new JSONArray(formats);
        JSONObject variables = new JSONObject();
        variables.put("query", search);
        variables.put("page", page);
        variables.put("formats", jsonFormats);


        //get anime list from AniList API
        HttpResponse<String> result = Unirest.post(AniList.GRAPHQL.link)
                .queryString("query", AniListQuery.SEARCH.getQuery())
                .queryString("variables", variables)
                .asString();

        log.debug(result.getBody());

        if(result.getStatus() == 429){
            String secs = result.getHeaders().getFirst("Retry-After");
            int seconds = Integer.parseInt(secs);
            log.warn("delay in {} seconds", seconds);
            throw new TooManyAnimeRequestsException(seconds);
        }else if(result.getStatus() == 404){
            log.error("Anime weren't found("+search+");");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }else if(result.getStatus() == 400){
            log.error("Something went wrong:\n"+ AniList.GRAPHQL+"\n"+AniListQuery.ANIME_BY_ID+"\n"+variables);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new JSONObject(result.getBody()).getJSONObject("data").getJSONObject("Page");
    }
    /**
     * Get list of anime trends. Cacheable as "animeTrends".
     * @return "Page" JSONObject which have page info(number, count, etc.) and JSONArray of anime "Media" JSONObjects.
     * @throws TooManyAnimeRequestsException if Anilist.co have got more requests than limit from app's account.
     * @throws NoAccessException if app can't access to Anilist.co API.
     */
    @Cacheable("animeTrends")
    public JSONObject getAnimeTrends() throws TooManyAnimeRequestsException, NoAccessException {
        log.info("Get anime trends");
        HttpResponse<String> result;
        try {
            result = Unirest.post(AniList.GRAPHQL.link)
                    .queryString("query", AniListQuery.ANIME_TRENDING.getQuery())
                    .asString();
        }catch (Exception e){
            throw new NoAccessException("No Access to Anilist.co");
        }
        log.debug(result.getBody());

        if(result.getStatus() == 429){
            String secs = result.getHeaders().getFirst("Retry-After");
            int seconds = Integer.parseInt(secs);
            log.warn("delay in {} seconds", seconds);
            throw new TooManyAnimeRequestsException(seconds);
        }else if(result.getStatus() == 400){
            log.error("Something went wrong:\n"+ AniList.GRAPHQL+"\n"+AniListQuery.ANIME_BY_ID+"\n");
            throw new NoAccessException("No Access to Anilist.co");
        }

        return new JSONObject(result.getBody()).getJSONObject("data").getJSONObject("Page");
    }
    /**
     * Get anime by id. Cacheable as "jsonAnime".
     * @param id identification number from Anilist database.
     * @return "Media" JSONObject with info about anime (id, title, description, etc.).
     * @throws TooManyAnimeRequestsException if Anilist.co have got more requests than limit from app's account.
     * @throws ContentNotFoundException if there's no such anime in database.
     */
    @Cacheable("jsonAnime")
    public JSONObject getAnimeById(int id) throws TooManyAnimeRequestsException, ContentNotFoundException {
        log.info("Get Anime: {}", id);
        String variables = "{\n" +
                "  \"id\": "+id+"\n" +
                "}";

        //get anime from AniList API
        HttpResponse<String> anime = Unirest.post(AniList.GRAPHQL.link)
                .queryString("query", AniListQuery.ANIME_BY_ID.getQuery())
                .queryString("variables", variables)
                .asString();

        log.debug(anime.getBody());

        if(anime.getStatus() == 429){
            int seconds = Integer.parseInt(anime.getHeaders().getFirst("Retry-After"));
            log.warn("delay in {} seconds", seconds);
            throw new TooManyAnimeRequestsException(seconds);
        }else if(anime.getStatus() == 404){
            log.error("Anime {} not found", id);
            throw new ContentNotFoundException("Anime #"+id+" not found");
        }else if(anime.getStatus() == 400){
            log.error("Something went wrong:\n"+ AniList.GRAPHQL+"\n"+AniListQuery.ANIME_BY_ID+"\n"+variables);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new JSONObject(anime.getBody()).getJSONObject("data").getJSONObject("Media");
    }
    /**
     * Get anime by name. Cacheable as "jsonAnime".
     * @param name title from Anilist database
     * @return "Media" JSONObject with info about anime (id, title, description, etc.).
     * @throws TooManyAnimeRequestsException if Anilist.co have got more requests than limit from app's account.
     * @throws ContentNotFoundException if there's no such anime in database.
     */
    @Cacheable("jsonAnime")
    public JSONObject getAnimeByName(String name) throws TooManyAnimeRequestsException, ContentNotFoundException {
        log.info("Get Anime: {}", name);
        String variables = "{\n" +
                "  \"name\": \""+name+"\"\n" +
                "}";

        //get anime from AniList API
        HttpResponse<String> anime = Unirest.post(AniList.GRAPHQL.link)
                .queryString("query", AniListQuery.ANIME_BY_NAME.getQuery())
                .queryString("variables", variables)
                .asString();

        log.debug(anime.getBody());

        if(anime.getStatus() == 429){
            int seconds = Integer.parseInt(anime.getHeaders().getFirst("Retry-After"));
            log.warn("delay in {} seconds", seconds);
            throw new TooManyAnimeRequestsException(seconds);
        }else if(anime.getStatus() == 404){
            log.error("Anime \"{}\" not found", name);
            throw new ContentNotFoundException("Anime '"+name+"' not found");
        }else if(anime.getStatus() == 400){
            log.error("Something went wrong:\n"+ AniList.GRAPHQL+"\n"+AniListQuery.ANIME_BY_NAME+"\n"+variables);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new JSONObject(anime.getBody()).getJSONObject("data").getJSONObject("Media");
    }
}
