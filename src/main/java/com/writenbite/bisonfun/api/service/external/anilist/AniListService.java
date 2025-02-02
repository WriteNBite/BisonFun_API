package com.writenbite.bisonfun.api.service.external.anilist;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.service.external.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.service.external.AnimeService;
import com.writenbite.bisonfun.api.service.RawVideoContentFactory;
import com.writenbite.bisonfun.api.service.external.database.VideoContentEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.writenbite.bisonfun.api.service.VideoContentServiceUtils.isNotNullAndPositive;

@Slf4j
@Service
public class AniListService implements AnimeService<AniListMedia, VideoContent> {

    private final static int ANILIST_ID_DEFAULT = -1;
    private final static int MAL_ID_DEFAULT = -1;
    private final AniListClient aniListClient;
    private final RawVideoContentFactory rawVideoContentFactory;
    private final VideoContentEntityService videoContentEntityService;

    public AniListService(AniListClient aniListClient, RawVideoContentFactory rawVideoContentFactory, VideoContentEntityService videoContentEntityService) {
        this.aniListClient = aniListClient;
        this.rawVideoContentFactory = rawVideoContentFactory;
        this.videoContentEntityService = videoContentEntityService;
    }

    @Override
    public boolean checkAndUpdateExternalIds(VideoContent videoContent) {
        boolean isChanged = false;
        try {
            if (videoContent.getAniListId() == null) {// if Anilist id is unchecked
                updateAniListId(videoContent);
                isChanged = true;
            }
            if (videoContent.getMalId() == null) {// if Mal id is unchecked
                updateMalId(videoContent);
                isChanged = true;
            }
        } catch (ContentNotFoundException e) {
            log.warn(e.getMessage());
            if(videoContent.getAniListId() == null) {
                videoContent.setAniListId(ANILIST_ID_DEFAULT);
            } else if (videoContent.getMalId() == null) {
                videoContent.setMalId(MAL_ID_DEFAULT);
            }
            isChanged = true;
        } catch (TooManyAnimeRequestsException e) {
            log.error(e.getMessage());
        }
        return isChanged;
    }

    /**
     * @param videoContent video content from database which need anilist id to be checked
     * @throws ContentNotFoundException proper content couldn't be found on anilist
     * @throws TooManyAnimeRequestsException when service reached rate limit of anilist
     * @see #isConflictingContent(AniListMedia, String)
     */
    private void updateAniListId(VideoContent videoContent) throws ContentNotFoundException, TooManyAnimeRequestsException {
        AniListMedia anime = aniListClient.parseAnimeByName(videoContent.getTitle());
        if(isConflictingContent(anime, videoContent.getTitle())){
            throw new ContentNotFoundException();
        }
        videoContent.setAniListId(anime.id());
    }

    /**
     * @param videoContent video content from database which need mal id to be checked
     * @throws ContentNotFoundException proper content couldn't be found on anilist
     * @throws TooManyAnimeRequestsException when service reached rate limit of anilist
     * @see #isConflictingContent(AniListMedia, String)
     */
    private void updateMalId(VideoContent videoContent) throws ContentNotFoundException, TooManyAnimeRequestsException {
        AniListMedia anime = isNotNullAndPositive(videoContent.getAniListId()) ? aniListClient.parseAnimeById(videoContent.getAniListId()) : aniListClient.parseAnimeByName(videoContent.getTitle());
        if(isConflictingContent(anime, videoContent.getTitle())){
            throw new ContentNotFoundException();
        }
        videoContent.setMalId(anime.idMal() != null ? anime.idMal() : MAL_ID_DEFAULT);
    }

    @Override
    public boolean isConflictingContent(AniListMedia anime, String title) {
        if(anime != null) {
            String animeTitle = anime.title().english() != null ? anime.title().english() : (anime.title().romaji() != null ? anime.title().romaji() : anime.title().nativeTitle());
            return !(animeTitle.equalsIgnoreCase(title) || anime.synonyms().contains(title));
        }
        return false;
    }

    @Override
    public Optional<AniListMedia> fetchAnimeContentById(Integer externalAnimeId) throws TooManyAnimeRequestsException {
        try {
            return Optional.ofNullable(aniListClient.parseAnimeById(externalAnimeId));
        } catch (ContentNotFoundException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<AniListMedia> fetchAnimeContentByTitle(String title) throws TooManyAnimeRequestsException {
        try {
            AniListMedia anime = aniListClient.parseAnimeByName(title);
            if(anime != null) {
                if(!isConflictingContent(anime, title)){
                    return Optional.of(anime);
                }else{
                    log.error("{} and found anime (MAL id: {}) are related to different video content", title, anime.idMal());
                }
            }
        } catch (ContentNotFoundException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getAnimeTrends() throws TooManyAnimeRequestsException {
        AniListPage<AniListMedia> aniListMedia = aniListClient.parseAnimeTrends();
        Map<AniListMedia, Optional<VideoContent>> aniListMediaVideoContentMap = videoContentEntityService.getAnimeContentMap(aniListMedia.getList());
        return aniListMediaVideoContentMap.keySet()
                .stream()
                .map(media -> rawVideoContentFactory.toBasicInfo(aniListMediaVideoContentMap.getOrDefault(media, null).orElse(null), media))
                .collect(Collectors.toList());
    }
}
