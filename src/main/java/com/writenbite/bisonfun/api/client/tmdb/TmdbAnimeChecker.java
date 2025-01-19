package com.writenbite.bisonfun.api.client.tmdb;

import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import info.movito.themoviedbapi.model.keywords.Keyword;

import java.util.List;

public class TmdbAnimeChecker {

    /**
     * @param content response class from themoviedb
     * @return <code>true</code> if one of the tmdb content exist and have 'Anime' keyword and <code>false</code> otherwise
     * @see #animeKeywordCheck(List)
     */
    public static boolean isTmdbContentAnime(TmdbVideoContent content) {
        return content != null && content.getKeywords() != null && animeKeywordCheck(content.getKeywords());
    }

    /**
     * @param keywords list of keywords from tmdb content
     * @return <code>true</code> if list of keywords contains 'anime' keyword
     */
    public static boolean animeKeywordCheck(List<Keyword> keywords){
        if (!keywords.isEmpty()) {
            return keywords.stream().anyMatch(keyword -> "anime".equalsIgnoreCase(keyword.getName()));
        }
        return false;
    }
}
