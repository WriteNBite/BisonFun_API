package com.writenbite.bisonfun.api.client.anilist;

import com.google.gson.Gson;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaPage;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListTrends;
import com.writenbite.bisonfun.api.service.external.TooManyAnimeRequestsException;
import kong.unirest.core.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class AniListClient {
    private final AniListApiResponse aniListApiResponse;
    private final Gson gson;

    @Autowired
    public AniListClient(AniListApiResponse aniListApiResponse, Gson gson) {
        this.aniListApiResponse = aniListApiResponse;
        this.gson = gson;
    }

    //parse anime lists
    public AniListPage<AniListMedia> parse(String query) throws TooManyAnimeRequestsException {
        return parse(query, 1, List.of(AniListMediaFormat.values()));
    }
    public AniListPage<AniListMedia> parse(String query, int page, Collection<AniListMediaFormat> formats) throws TooManyAnimeRequestsException {
        log.info("Parsing video entertainment anime:\nquery:{}\npage{}", query, page);
        JSONObject root = aniListApiResponse.getAnimeList(query, page, formats);

        AniListMediaPage mediaPage = gson.fromJson(String.valueOf(root), AniListMediaPage.class);

        log.debug("Media Page: {}", mediaPage);
        return mediaPage;
    }
    //parse anime trends
    @Cacheable("animeTrends")
    public AniListTrends parseAnimeTrendsList() throws TooManyAnimeRequestsException {
        log.debug("Parsing Anime Trends List");
        JSONObject root;
        try {
            root = aniListApiResponse.getAnimeTrendsList();
        } catch (NoAccessException e) {
            log.error(e.getMessage());
            return null;
        }
        AniListTrends aniListTrends = gson.fromJson(String.valueOf(root), AniListTrends.class);

        log.debug("AniListTrends: {}", aniListTrends);
        return aniListTrends;
    }

    //parse anime(as VideoEntertainment)
    @Cacheable("jsonAnime")
    public AniListMedia parseAnimeById(Integer aniListId) throws ContentNotFoundException, TooManyAnimeRequestsException {
        if(aniListId != null) {
            JSONObject jsonAnime = aniListApiResponse.getAnimeById(aniListId);
            return parseAnime(jsonAnime);
        }
        return null;
    }
    @Cacheable("jsonAnime")
    public AniListMedia parseAnimeByName(String name) throws ContentNotFoundException, TooManyAnimeRequestsException {
        JSONObject jsonAnime = aniListApiResponse.getAnimeByName(name);
        return parseAnime(jsonAnime);
    }
    private AniListMedia parseAnime(JSONObject jsonAnime){
        return gson.fromJson(String.valueOf(jsonAnime), AniListMedia.class);
    }

}
