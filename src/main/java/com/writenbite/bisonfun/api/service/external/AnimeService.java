package com.writenbite.bisonfun.api.service.external;

import com.writenbite.bisonfun.api.types.videocontent.VideoContent;

import java.util.List;
import java.util.Optional;

public interface AnimeService<Anime, DatabaseVideoContent> {

    /**
     * @param databaseVideoContent video content from database which need external ids to be checked
     * @return <code>true</code> if video content had unchecked external ids, it was updated and <code>false</code> otherwise
     */
    boolean checkAndUpdateExternalIds(DatabaseVideoContent databaseVideoContent);

    /**
     * @param anime class from external source
     * @param title title need to check if it's part of anime's titles
     * @return <code>true</code> when there's titles aren't matching and <code>false</code> when there's no conflict
     */
    boolean isConflictingContent(Anime anime, String title);

    Optional<Anime> fetchAnimeContentById(Integer externalAnimeId) throws TooManyAnimeRequestsException;

    Optional<Anime> fetchAnimeContentByTitle(String title) throws TooManyAnimeRequestsException;

    List<VideoContent.BasicInfo> getAnimeTrends() throws TooManyAnimeRequestsException;
}
