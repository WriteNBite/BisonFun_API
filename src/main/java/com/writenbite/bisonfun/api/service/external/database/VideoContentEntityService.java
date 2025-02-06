package com.writenbite.bisonfun.api.service.external.database;

import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.database.repository.VideoContentRepository;
import com.writenbite.bisonfun.api.service.external.DatabaseService;
import com.writenbite.bisonfun.api.service.UpdateVideoContentResponse;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.writenbite.bisonfun.api.service.VideoContentEntityUpdater.*;

@Slf4j
@Service
public class VideoContentEntityService implements DatabaseService<VideoContent, TmdbSimpleVideoContent, AniListMedia> {
    private final VideoContentRepository videoContentRepository;
    private final VideoContentFormatMapper videoContentFormatMapper;

    public VideoContentEntityService(VideoContentRepository videoContentRepository, VideoContentFormatMapper videoContentFormatMapper) {
        this.videoContentRepository = videoContentRepository;
        this.videoContentFormatMapper = videoContentFormatMapper;
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return existed video content from database or <code>Optional.null()</code> otherwise
     */
    @Override
    public Optional<VideoContent> getVideoContentDb(VideoContentIdInput input) {
        if (input.videoContentId() != null) {
            return videoContentRepository.findById(input.videoContentId());
        }
        if (input.aniListId() != null) {
            return videoContentRepository.findByAniListId(input.aniListId());
        }
        if (input.tmdbVideoContentIdInput() != null) {
            return videoContentRepository.findByTmdbIdAndType(input.tmdbVideoContentIdInput().tmdbId(), videoContentFormatMapper.toVideoContentType(input.tmdbVideoContentIdInput().format()));
        }
        return Optional.empty();
    }

    @Override
    public VideoContent saveVideoContent(VideoContent videoContent) {
        return videoContentRepository.save(videoContent);
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return <code>true</code> if video content is existed in database and <code>false</code> otherwise
     */
    @Override
    public boolean checkVideoContentExistence(VideoContentIdInput input){
        boolean existence = false;
        if(input.aniListId() != null && input.aniListId() > 0){
            existence = videoContentRepository.existsByAniListId(input.aniListId());
        }
        if(input.tmdbVideoContentIdInput() != null && input.tmdbVideoContentIdInput().tmdbId() > 0){
            existence = existence || videoContentRepository.existsByTmdbIdAndType(input.tmdbVideoContentIdInput().tmdbId(), videoContentFormatMapper.toVideoContentType(input.tmdbVideoContentIdInput().format()));
        }
        return existence;
    }

    @Override
    public Map<TmdbSimpleVideoContent, Optional<VideoContent>> getMainstreamContentMap(List<TmdbSimpleVideoContent> tmdbList){
        Map<VideoContentType, Set<TmdbSimpleVideoContent>> typeSetMap = tmdbList.stream()
                .collect(Collectors.groupingBy(
                                TmdbSimpleVideoContent::getVideoContentType,
                                Collectors.toSet()
                        )
                );
        Map<TmdbSimpleVideoContent, Optional<VideoContent>> videoContentMap = new HashMap<>();
        for(Map.Entry<VideoContentType, Set<TmdbSimpleVideoContent>> typeSet : typeSetMap.entrySet()){
            Map<TmdbSimpleVideoContent, Optional<VideoContent>> map = getTmdbTypedVideoContentMap(typeSet.getValue(), typeSet.getKey());
            videoContentMap.putAll(
                    map.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
        }
        return videoContentMap;
    }

    private Map<TmdbSimpleVideoContent, Optional<VideoContent>> getTmdbTypedVideoContentMap(Collection<TmdbSimpleVideoContent> tmdbList, VideoContentType videoContentType) {
        if(tmdbList == null){
            return Collections.emptyMap();
        }

        // Collect tmdbIds from tmdbList, ensuring no null ids
        Map<Integer, TmdbSimpleVideoContent> tmdbIds = tmdbList.stream()
                .collect(Collectors.toMap(
                        TmdbSimpleVideoContent::getTmdbId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        // Fetch VideoContent list from repository
        List<VideoContent> tmdbTypedVideoContentList = videoContentRepository.findListByTmdbIdsAndType(tmdbIds.keySet(), videoContentType);

        // Null check for tmdbTypedVideoContentList, assuming it returns empty list if no data
        if(tmdbTypedVideoContentList == null){
            tmdbTypedVideoContentList = Collections.emptyList();
        }

        // Create a map from tmdbId to VideoContent
        Map<Integer, VideoContent> idVideoContentMap = tmdbTypedVideoContentList.stream()
                .collect(Collectors.toMap(
                        VideoContent::getTmdbId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        // Create the final map from TmdbSimpleVideoContent to VideoContent
        Map<TmdbSimpleVideoContent, Optional<VideoContent>> tmdbSimpleVideoContentMap = new HashMap<>();
        for (Integer tmdbId : tmdbIds.keySet()) {
            TmdbSimpleVideoContent tmdbSimpleVideoContent = tmdbIds.get(tmdbId);
            Optional<VideoContent> optionalVideoContent = Optional.ofNullable(idVideoContentMap.getOrDefault(tmdbId, null));
            tmdbSimpleVideoContentMap.put(tmdbSimpleVideoContent, optionalVideoContent);
        }
        return tmdbSimpleVideoContentMap;
    }

    @Override
    public Map<AniListMedia, Optional<VideoContent>> getAnimeContentMap(List<AniListMedia> aniListMedia) {
        Map<Integer, AniListMedia> aniListIds = aniListMedia.stream()
                .collect(Collectors.toMap(
                        AniListMedia::id,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        List<VideoContent> animeVideoContent = videoContentRepository.findListByAniListIds(aniListIds.keySet());
        Map<Integer, VideoContent> animeMap = animeVideoContent.stream()
                .collect(
                        Collectors.toMap(
                                VideoContent::getAniListId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        )
                );
        Map<AniListMedia, Optional<VideoContent>> animeVideoContentMap = new HashMap<>();
        for (Integer aniListId : aniListIds.keySet()) {
            animeVideoContentMap.put(aniListIds.get(aniListId), Optional.ofNullable(animeMap.getOrDefault(aniListId, null)));
        }
        return animeVideoContentMap;
    }

    private Optional<VideoContent> getVideoContentByVideoContent(@NonNull VideoContent videoContent) {
        if (videoContent.getId() != null) {
            return videoContentRepository.findById(videoContent.getId());
        } else if (videoContent.getAniListId() != null) {
            return videoContentRepository.findByAniListId(videoContent.getAniListId());
        } else if (videoContent.getTmdbId() != null) {
            return videoContentRepository.findByTmdbIdAndType(videoContent.getTmdbId(), videoContent.getType());
        }
        return Optional.empty();
    }

    /**
     * @param updatedVideoContent video content with new data
     * @return response with <code>changesMade</code> boolean value that return <code>true</code> if there was at least one updated value and updated video content if it existed before in database, or <code>Optional.null()</code> otherwise
     * @see #getVideoContentByVideoContent(VideoContent)
     * @see com.writenbite.bisonfun.api.service.VideoContentEntityUpdater#updatePoster(VideoContent, VideoContent)
     * @see com.writenbite.bisonfun.api.service.VideoContentEntityUpdater#updateCategory(VideoContent, VideoContent)
     * @see com.writenbite.bisonfun.api.service.VideoContentEntityUpdater#updateType(VideoContent, VideoContent)
     * @see com.writenbite.bisonfun.api.service.VideoContentEntityUpdater#updateYear(VideoContent, VideoContent)
     */
    @Override
    public UpdateVideoContentResponse<Optional<VideoContent>> updateContent(VideoContent updatedVideoContent) {
        AtomicBoolean updated = new AtomicBoolean(false);
        return new UpdateVideoContentResponse<>(
                getVideoContentByVideoContent(updatedVideoContent)
                .map(existingVideoContent -> {
                    updated.set(updatePoster(existingVideoContent, updatedVideoContent) | updated.get());
                    updated.set(updateCategory(existingVideoContent, updatedVideoContent) | updated.get());
                    updated.set(updateType(existingVideoContent, updatedVideoContent) | updated.get());
                    updated.set(updateYear(existingVideoContent, updatedVideoContent) | updated.get());

                    return videoContentRepository.save(existingVideoContent);
                }),
                updated.get()
        );
    }
}
