package com.writenbite.bisonfun.api.service.external;

import com.writenbite.bisonfun.api.service.UpdateVideoContentResponse;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DatabaseService<DatabaseVideoContent, MainstreamVideoContent, Anime> {
    /**
     * @param input record with available ids for search
     * @return existed video content from database or <code>Optional.null()</code> otherwise
     */
    Optional<DatabaseVideoContent> getVideoContentDb(VideoContentIdInput input);

    DatabaseVideoContent saveVideoContent(DatabaseVideoContent videoContent);

    /**
     * @param input record with available ids for search
     * @return <code>true</code> if video content is existed in database and <code>false</code> otherwise
     */
    boolean checkVideoContentExistence(VideoContentIdInput input);

    Map<MainstreamVideoContent, Optional<DatabaseVideoContent>> getMainstreamContentMap(List<MainstreamVideoContent> mainstreamVideoContentList);

    Map<Anime, Optional<DatabaseVideoContent>> getAnimeContentMap(List<Anime> animeList);

    /**
     * @param updatedVideoContent video content with new data
     * @return response with <code>changesMade</code> boolean value that return <code>true</code> if there was at least one updated value and updated video content if it existed before in database, or <code>Optional.null()</code> otherwise
     */
    UpdateVideoContentResponse<Optional<DatabaseVideoContent>> updateContent(DatabaseVideoContent updatedVideoContent);
}
