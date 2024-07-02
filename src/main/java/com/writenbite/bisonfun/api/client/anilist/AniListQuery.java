package com.writenbite.bisonfun.api.client.anilist;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Getter
public enum AniListQuery {
    /**
     * Query to get logged-in user(viewer)'s name and id from Anilist.co
     **/
    VIEWER("graphql/anilist/viewer.graphql"),
    /**
     * Query to get user's anime lists based on user id, media status and page of list.
     */
    USER_LIST("graphql/anilist/user_list.graphql"),
    /**
     * Query to get anime list based on search query and page of list
     */
    SEARCH("graphql/anilist/search.graphql"),
    /**
     * Query to get anime by id from Anilist.co
     */
    ANIME_BY_ID("graphql/anilist/anime_by_id.graphql"),
    /**
     * Query to get anime by name from Anilist.co
     */
    ANIME_BY_NAME("graphql/anilist/anime_by_name.graphql"),
    /**
     * Query to get trending anime
     */
    ANIME_TRENDING("graphql/anilist/anime_trending.graphql");

    @SneakyThrows
    AniListQuery(String classpath){
        Resource resource = new ClassPathResource(classpath);
        InputStream inputStream = resource.getInputStream();
        query = inputStreamToString(inputStream);

    }
    private final String query;

    private String inputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }
}
