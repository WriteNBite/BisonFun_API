package com.writenbite.bisonfun.api.service.external;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.tmdb.TmdbAnimeChecker;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import com.writenbite.bisonfun.api.types.videocontent.input.TmdbVideoContentIdInput;

import java.util.List;
import java.util.Optional;

public interface MainstreamService<MainstreamVideoContent, DatabaseVideoContent, AnimeVideoContent> {

    /**
     * @param databaseVideoContent which need external ids to be checked
     * @return <code>true</code> if video content had unchecked external ids, it was updated and <code>false</code> otherwise
     */
    boolean checkAndUpdateExternalIds(DatabaseVideoContent databaseVideoContent);

    /**
     * @param mainstreamVideoContent response class
     * @return <code>true</code> if one of the mainstream content exist and have 'Anime characteristics' and <code>false</code> otherwise
     * @see TmdbAnimeChecker#animeKeywordCheck(List)
     */
    boolean isMainstreamContentAnime(MainstreamVideoContent mainstreamVideoContent);

    /**
     * @param input record class which contains id of themoviedb and type of content
     * @return mainstream content
     */
    Optional<MainstreamVideoContent> fetchMainstreamContentById(TmdbVideoContentIdInput input) throws ContentNotFoundException;

    /**
     * @param anime record class
     * @return mainstream video content if it has been found or empty Optional otherwise
     */
    Optional<MainstreamVideoContent> fetchMainstreamContentByAnime(AnimeVideoContent anime);

    List<VideoContent.BasicInfo> getTrends(VideoContentFormat format) throws ContentNotFoundException;

}
