package com.writenbite.bisonfun.api;

import com.google.gson.Gson;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListTrends;
import kong.unirest.core.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AniListJsonConversionTest {

    String konosuba;
    String animeTrends;
    @BeforeEach
    public void jsonEntities() throws IOException {
        Resource konosubaResource = new ClassPathResource("konosuba_3.json");
        Resource animeTrendsResources = new ClassPathResource("animeTrends.json");
        konosuba = new String(Files.readAllBytes(Paths.get(konosubaResource.getURI())));
        animeTrends = new String(Files.readAllBytes(Paths.get(animeTrendsResources.getURI())));
    }
    @Test
    public void givenJsonString_whenDeserialized_thenMediaRecordCreated(){
        AniListMedia media = new Gson().fromJson(konosuba, AniListMedia.class);
        JSONObject jsonObject = new JSONObject(konosuba);

        assertEquals(jsonObject.getInt("id"),media.id());
        assertEquals(jsonObject.getInt("idMal"),media.idMal());
        assertEquals(jsonObject.getJSONObject("title").getString("romaji"), media.title().romaji());
        assertEquals(jsonObject.getJSONObject("title").getString("english"), media.title().english());
        assertEquals(jsonObject.getString("format"), media.format().toString());
        assertEquals(jsonObject.getString("status"), media.status().toString());
        assertEquals(jsonObject.getString("description"), media.description());
        assertEquals(jsonObject.getJSONObject("startDate").getInt("year"), media.startDate().year());
        assertEquals(jsonObject.getJSONObject("startDate").getInt("month"), media.startDate().month());
        assertEquals(jsonObject.getJSONObject("startDate").getInt("day"), media.startDate().day());
        assertEquals(jsonObject.getInt("seasonYear"), media.seasonYear());
        assertEquals(jsonObject.getInt("episodes"), media.episodes());
        assertEquals(jsonObject.getInt("duration"), media.duration());

        assertEquals(jsonObject.getString("countryOfOrigin"), media.countryOfOrigin());
        assertEquals(jsonObject.getBoolean("isLicensed"), media.isLicensed());
        assertEquals(jsonObject.getInt("updatedAt"), media.updatedAt());
        assertEquals(jsonObject.getJSONObject("coverImage").getString("medium"), media.coverImage().medium());
        assertEquals(jsonObject.getJSONObject("coverImage").getString("large"), media.coverImage().large());
        assertEquals(jsonObject.getString("bannerImage"), media.bannerImage());
        assertArrayEquals(jsonObject.getJSONArray("genres").toList().toArray(), media.genres().toArray());
        assertArrayEquals(jsonObject.getJSONArray("synonyms").toList().toArray(), media.synonyms().toArray());
        assertEquals(jsonObject.getInt("averageScore"), media.averageScore());
        assertEquals(jsonObject.getInt("meanScore"), media.meanScore());
        assertEquals(jsonObject.getInt("popularity"), media.popularity());
        assertEquals(jsonObject.getBoolean("isLocked"), media.isLocked());
        assertEquals(jsonObject.getInt("trending"), media.trending());
        assertEquals(jsonObject.getInt("favourites"), media.favourites());
    }

    @Test
    public void givenJsonString_whenDeserialized_thenMediaListCreated(){
        AniListTrends trends = new Gson().fromJson(animeTrends, AniListTrends.class);
        assertNotNull(trends.movieTrends().getList());
        assertNotNull(trends.movieTrends().getPageInfo());
    }
}
