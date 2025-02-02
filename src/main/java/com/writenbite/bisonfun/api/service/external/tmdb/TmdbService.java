package com.writenbite.bisonfun.api.service.external.tmdb;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaTitleMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContentResultsPage;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.database.mapper.VideoContentTypeMapper;
import com.writenbite.bisonfun.api.service.external.MainstreamService;
import com.writenbite.bisonfun.api.service.RawVideoContentFactory;
import com.writenbite.bisonfun.api.service.VideoContentServiceUtils;
import com.writenbite.bisonfun.api.service.external.database.VideoContentEntityService;
import com.writenbite.bisonfun.api.types.mapper.TmdbIdInputMapper;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.writenbite.bisonfun.api.client.tmdb.TmdbAnimeChecker.isTmdbContentAnime;
import static com.writenbite.bisonfun.api.service.VideoContentServiceUtils.isConflictingContent;
import static com.writenbite.bisonfun.api.service.VideoContentServiceUtils.isNotNullAndPositive;

@Slf4j
@Service
public class TmdbService implements MainstreamService<TmdbVideoContent, VideoContent, AniListMedia> {

    private final static int TMDB_ID_DEFAULT = -1;
    private final static String IMDB_ID_DEFAULT = "";

    private final VideoContentTypeMapper videoContentTypeMapper;
    private final AniListMediaTitleMapper aniListMediaTitleMapper;
    private final TmdbClient tmdbClient;
    private final RawVideoContentFactory rawVideoContentFactory;
    private final TmdbIdInputMapper tmdbIdInputMapper;
    private final VideoContentEntityService videoContentEntityService;

    public TmdbService(VideoContentTypeMapper videoContentTypeMapper, AniListMediaTitleMapper aniListMediaTitleMapper, TmdbClient tmdbClient, RawVideoContentFactory rawVideoContentFactory, TmdbIdInputMapper tmdbIdInputMapper, VideoContentEntityService videoContentEntityService) {
        this.videoContentTypeMapper = videoContentTypeMapper;
        this.aniListMediaTitleMapper = aniListMediaTitleMapper;
        this.tmdbClient = tmdbClient;
        this.rawVideoContentFactory = rawVideoContentFactory;
        this.tmdbIdInputMapper = tmdbIdInputMapper;
        this.videoContentEntityService = videoContentEntityService;
    }

    @Override
    public boolean checkAndUpdateExternalIds(VideoContent videoContent) {
        boolean isChanged = false;
        try {
            if (videoContent.getTmdbId() == null) {// if TmdbId is unchecked set tmdb id
                updateTmdbId(videoContent);
                isChanged = true;
            }
            if (videoContent.getImdbId() == null) {// if ImdbId is unchecked
                updateImdbId(videoContent);
                isChanged = true;
            }
        } catch (ContentNotFoundException e) {
            log.warn(e.getMessage());
            if (videoContent.getTmdbId() == null){
                videoContent.setTmdbId(TMDB_ID_DEFAULT);
            } else if(videoContent.getImdbId() == null) {
                videoContent.setImdbId(IMDB_ID_DEFAULT);
            }
            isChanged = true;
        }
        return isChanged;
    }

    /**
     * @param videoContent video content from database which need themoviedb id to be checked
     * @throws ContentNotFoundException proper content couldn't be found on themoviedb
     * @see #isConflictingTmdbContent(VideoContent, TmdbSimpleVideoContent)
     */
    private void updateTmdbId(VideoContent videoContent) throws ContentNotFoundException {
        TmdbSimpleVideoContent tmdbSimpleVideoContent = switch (videoContent.getType()){
            case TV -> tmdbClient.parseTmdbTvByName(videoContent.getTitle(), videoContent.getYear());
            case MOVIE -> tmdbClient.parseTmdbMovieByName(videoContent.getTitle(), videoContent.getYear());
            default -> null;
        };

        if (tmdbSimpleVideoContent == null){
            return;
        }

        if (isConflictingTmdbContent(videoContent, tmdbSimpleVideoContent)){
            throw new ContentNotFoundException();
        }

        videoContent.setTmdbId(tmdbSimpleVideoContent.getTmdbId());
    }

    /**
     * @param videoContent video content from database which need imdb id to be checked
     * @throws ContentNotFoundException proper content couldn't be found on themoviedb
     * @see VideoContentServiceUtils#isConflictingContent(String, String)
     */
    private void updateImdbId(VideoContent videoContent) throws ContentNotFoundException {
        String imdbId = IMDB_ID_DEFAULT;

        if(isNotNullAndPositive(videoContent.getTmdbId())) {
            TmdbVideoContent tmdbVideoContent = switch (videoContent.getType()){
                case TV -> tmdbClient.parseShowById(videoContent.getTmdbId());
                case MOVIE -> tmdbClient.parseMovieById(videoContent.getTmdbId());
                default -> null;
            };

            if (tmdbVideoContent == null){
                return;
            }

            if(isConflictingTmdbContent(videoContent, tmdbVideoContent)){
                throw new ContentNotFoundException();
            }

            if(tmdbVideoContent.getImdbId().isPresent()){
                imdbId = tmdbVideoContent.getImdbId().get();
            }

            videoContent.setImdbId(imdbId);
        }
    }

    private boolean isConflictingTmdbContent(VideoContent videoContent, TmdbSimpleVideoContent tmdbSimpleVideoContent) {
        return isConflictingContent(videoContent.getTitle(), tmdbSimpleVideoContent.getTitle()) || isConflictingContent(videoContent.getTitle(), tmdbSimpleVideoContent.getOriginalTitle());
    }

    @Override
    public boolean isMainstreamContentAnime(TmdbVideoContent tmdbVideoContent){
        return isTmdbContentAnime(tmdbVideoContent);
    }

    @Override
    public Optional<TmdbVideoContent> fetchMainstreamContentById(com.writenbite.bisonfun.api.types.videocontent.input.TmdbVideoContentIdInput tmdbVideoContentIdInput) throws ContentNotFoundException {
        return fetchTmdbContentById(tmdbIdInputMapper.fromInputType(tmdbVideoContentIdInput));
    }

    @Override
    public Optional<TmdbVideoContent> fetchMainstreamContentByAnime(AniListMedia anime){
        VideoContentType videoContentType = videoContentTypeMapper.fromAniListMediaFormat(anime.format());
        Integer year = anime.startDate() != null ? anime.startDate().year() : null;
        return fetchTmdbContentByName(new TmdbVideoContentTitleInput(aniListMediaTitleMapper.animeEnglishTitle(anime.title()), videoContentType, year));
    }

    private Optional<TmdbVideoContent> fetchTmdbContentById(TmdbVideoContentIdInput tmdbVideoContentIdInput) throws ContentNotFoundException {
        return switch (tmdbVideoContentIdInput.type()){
            case TV -> Optional.ofNullable(tmdbClient.parseShowById(tmdbVideoContentIdInput.tmdbId()));
            case MOVIE -> Optional.ofNullable(tmdbClient.parseMovieById(tmdbVideoContentIdInput.tmdbId()));
            default -> Optional.empty();
        };
    }

    private Optional<TmdbVideoContent> fetchTmdbContentByName(TmdbVideoContentTitleInput tmdbTitleInput) {
        try {
            TmdbSimpleVideoContent tmdbSimpleVideoContent = switch (tmdbTitleInput.contentType()) {
                case TV -> tmdbClient.parseTmdbTvByName(tmdbTitleInput.title(), tmdbTitleInput.year());
                case MOVIE -> tmdbClient.parseTmdbMovieByName(tmdbTitleInput.title(), tmdbTitleInput.year());
                default -> null;
            };
            if(tmdbSimpleVideoContent != null && isNotNullAndPositive(tmdbSimpleVideoContent.getTmdbId())){
                return fetchTmdbContentById(new TmdbVideoContentIdInput(tmdbSimpleVideoContent.getTmdbId(), tmdbSimpleVideoContent.getVideoContentType()));
            }
        }catch (ContentNotFoundException e){
            log.error(e.getMessage());
        }
        return Optional.empty();

    }

    @Override
    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getTrends(VideoContentFormat videoContentFormat) throws ContentNotFoundException {
        TmdbVideoContentResultsPage tmdbTrends;
        try {
            tmdbTrends = switch (videoContentFormat) {
                case TV: yield tmdbClient.parseTVTrends();
                case MOVIE: yield tmdbClient.parseMovieTrends();
                default: throw new IllegalStateException("Unexpected value: " + videoContentFormat);
            };
        } catch (NoAccessException e) {
            log.error(e.getMessage());
            throw new ContentNotFoundException();
        }
        Map<TmdbSimpleVideoContent, Optional<VideoContent>> movieMediaVideoContentMap = videoContentEntityService.getMainstreamContentMap(tmdbTrends.results());

        return movieMediaVideoContentMap.keySet()
                .stream()
                .map(movie -> rawVideoContentFactory.toBasicInfo(movieMediaVideoContentMap.get(movie).orElse(null), movie))
                .collect(Collectors.toList());
    }
}
