package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.TmdbAnimeChecker;
import com.writenbite.bisonfun.api.client.tmdb.mapper.TmdbVideoContentMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import com.writenbite.bisonfun.api.service.external.*;
import com.writenbite.bisonfun.api.types.videocontent.input.TmdbVideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static com.writenbite.bisonfun.api.service.VideoContentServiceUtils.isNotNullAndPositive;

@Slf4j
@Service
public class VideoContentService {
    private final AniListMediaMapper aniListMediaMapper;
    private final TmdbVideoContentMapper tmdbVideoContentMapper;

    @Value("${bisonfun.data-update.range-in-days:1}")
    private Integer dayRange;

    private final VideoContentFormatMapper videoContentFormatMapper;
    private final VideoContentMapper videoContentMapper;
    private final RawVideoContentFactory rawVideoContentFactory;
    private final VideoContentInputValidator inputValidator;
    private final DataIntegrityValidator<TmdbVideoContent, AniListMedia> mainstreamAnimeIntegrityValidator;

    //ServiceInterfaces
    private final DatabaseService<VideoContent, TmdbSimpleVideoContent, AniListMedia> databaseService;
    private final AnimeService<AniListMedia, VideoContent> animeService;
    private final MainstreamService<TmdbVideoContent, VideoContent, AniListMedia> mainstreamService;

    @Autowired
    public VideoContentService(VideoContentMapper videoContentMapper, VideoContentFormatMapper videoContentFormatMapper,
                               RawVideoContentFactory rawVideoContentFactory, AniListMediaMapper aniListMediaMapper, TmdbVideoContentMapper tmdbVideoContentMapper, VideoContentInputValidator inputValidator, DataIntegrityValidator<TmdbVideoContent, AniListMedia> mainstreamAnimeIntegrityValidator,
                               DatabaseService<VideoContent, TmdbSimpleVideoContent, AniListMedia> databaseService, AnimeService<AniListMedia, VideoContent> animeService, MainstreamService<TmdbVideoContent, VideoContent, AniListMedia> mainstreamService) {
        this.videoContentMapper = videoContentMapper;
        this.videoContentFormatMapper = videoContentFormatMapper;
        this.rawVideoContentFactory = rawVideoContentFactory;
        this.aniListMediaMapper = aniListMediaMapper;
        this.tmdbVideoContentMapper = tmdbVideoContentMapper;
        this.inputValidator = inputValidator;
        this.mainstreamAnimeIntegrityValidator = mainstreamAnimeIntegrityValidator;
        this.databaseService = databaseService;
        this.animeService = animeService;
        this.mainstreamService = mainstreamService;
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content in {@link com.writenbite.bisonfun.api.types.videocontent.VideoContent} record class for GraphQL response
     * @throws ExternalInfoException when service can't access to external sources and information
     * @throws ContentNotFoundException if content wasn't found
     * @throws TooManyAnimeRequestsException when service can't access to anilist due to big number of requests from service
     * @see #getOrCreateVideoContent(VideoContentIdInput)
     * @see #fetchVideoContent(VideoContentIdInput)
     */
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByIdInput(VideoContentIdInput input, boolean hasExternalInfo) throws ExternalInfoException, ContentNotFoundException, TooManyAnimeRequestsException {
        com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo basicInfo;
        com.writenbite.bisonfun.api.types.videocontent.VideoContent.ExternalInfo externalInfo = null;
        basicInfo = videoContentMapper.toBasicInfo(getOrCreateVideoContent(input));
        if (hasExternalInfo){
            VideoContentCompilation compilation;
            try {
                compilation = fetchVideoContent(input);
            } catch (TooManyAnimeRequestsException e) {
                log.error(e.getMessage());
                throw new ExternalInfoException(e.getMessage(), basicInfo);
            }
            if (compilation.aniListMedia() != null) {
                externalInfo = aniListMediaMapper.toExternalInfo(compilation.aniListMedia());
            } else if (compilation.tmdbVideoContent() != null) {
                externalInfo = tmdbVideoContentMapper.toExternalInfo(compilation.tmdbVideoContent());
            }
        }
        return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfo, externalInfo);
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return video content from database
     * @throws ContentNotFoundException if content couldn't be found in both external and inner sources
     * @throws TooManyAnimeRequestsException when service can't access to anilist due to big number of requests from service
     * @see VideoContentInputValidator#validateVideoContentIdInput(VideoContentIdInput)
     * @see #getExistingVideoContent(VideoContentIdInput)
     * @see #isRequireUpdate(VideoContent)
     * @see #fetchAndUpdateVideoContent(VideoContentIdInput)
     * @see #fetchAndSaveVideoContent(VideoContentIdInput)
     */
    public VideoContent getOrCreateVideoContent(final VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        inputValidator.validateVideoContentIdInput(input);

        Optional<VideoContent> optionalDatabaseVideoContent = databaseService.getVideoContentDb(input);
        if(optionalDatabaseVideoContent.isPresent()) {
            return handleExistingContent(optionalDatabaseVideoContent.get(), input);
        }else{
            return fetchAndSaveVideoContent(input);
        }
    }

    private VideoContent handleExistingContent(VideoContent videoContent, VideoContentIdInput videoContentIdInput){
        if(isRequireUpdate(videoContent)){
            try {
                return fetchAndUpdateVideoContent(videoContentIdInput);
            } catch (ContentNotFoundException | TooManyAnimeRequestsException e) {
                log.warn("Failed to update content ID: {}",videoContent.getId(), e );
                return videoContent;
            }
        }
        return videoContent;
    }

    /**
     * @param videoContent video content which need to check
     * @return <code>true</code> if video content was last updated more than {@link #dayRange} days ago and <code>false</code> otherwise
     */
    private boolean isRequireUpdate(VideoContent videoContent){
        return LocalDate.now().minusDays(dayRange).isAfter(videoContent.getLastUpdated());
    }

    /**
     * Return existing video content and update video content in case, if it contains unchecked ids (anilist, mal, themoviedb, imdb)
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return {@link Optional} with {@link VideoContent} if it exists in the database and <code>Optional.empty()</code> otherwise
     * @see DatabaseService#getVideoContentDb(VideoContentIdInput)
     * @see #checkAndUpdateExternalIds(VideoContent)
     */
    private Optional<VideoContent> getExistingVideoContent(VideoContentIdInput input){
        Optional<VideoContent> optionalVideoContent = databaseService.getVideoContentDb(input);
        if (optionalVideoContent.isPresent()) {
            VideoContent videoContent = optionalVideoContent.get();
            //Check video content ids
            if (checkAndUpdateExternalIds(videoContent)) {
                databaseService.saveVideoContent(videoContent);
            }
            return Optional.of(videoContent);
        }
        return Optional.empty();
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return video content gotten from external and inner sources and saved in database
     * @throws ContentNotFoundException if content couldn't be found in both external and inner sources
     * @throws TooManyAnimeRequestsException when service can't access to anilist due to big number of requests from service
     * @see #fetchVideoContent(VideoContentIdInput)
     */
    private VideoContent fetchAndSaveVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        VideoContentCompilation compilation = fetchVideoContent(input);
        VideoContent videoContent = rawVideoContentFactory.toVideoContentDb(
                compilation.aniListMedia(),
                compilation.tmdbVideoContent()
        );
        return databaseService.saveVideoContent(videoContent);
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return video content gotten from external and inner sources and updated in database
     * @throws ContentNotFoundException if content couldn't be found in both external and inner sources
     * @throws TooManyAnimeRequestsException when service can't access to anilist due to big number of requests from service
     * @see #fetchVideoContent(VideoContentIdInput)
     * @see #updateContent(VideoContent)
     */
    private VideoContent fetchAndUpdateVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        VideoContentCompilation fetchedContent = fetchVideoContent(input);
        VideoContent videoContent = rawVideoContentFactory.toVideoContentDb(fetchedContent.aniListMedia(), fetchedContent.tmdbVideoContent());
        return updateContent(videoContent).orElseThrow(() -> new ContentNotFoundException("Can't find video content by input: " + input));
    }

    /**
     * @param videoContent video content from database which need external ids (anilist, mal, themoviedb, imdb) to be checked
     * @return <code>true</code> if video content had unchecked external ids, it was updated and <code>false</code> otherwise
     * @see MainstreamService#checkAndUpdateExternalIds(Object)
     * @see AnimeService#checkAndUpdateExternalIds(Object)
     */
    public boolean checkAndUpdateExternalIds(VideoContent videoContent) {
        boolean isChanged = mainstreamService.checkAndUpdateExternalIds(videoContent);

        isChanged |= animeService.checkAndUpdateExternalIds(videoContent);

        return isChanged;
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return {@link VideoContentCompilation} record class which contains fetched video content (original from database, anilist and themoviedb)
     * @throws ContentNotFoundException proper content couldn't be found everywhere
     * @throws TooManyAnimeRequestsException when service reached rate limit set in anilist
     * @see #getExistingVideoContent(VideoContentIdInput)
     * @see MainstreamService#fetchMainstreamContentById(TmdbVideoContentIdInput)
     * @see MainstreamService#fetchMainstreamContentByAnime(Object)
     * @see TmdbAnimeChecker#isTmdbContentAnime(TmdbVideoContent)
     * @see AnimeService#isConflictingContent(Object, String)
     */
    public VideoContentCompilation fetchVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        IdResolutionResult ids = resolveIds(input);
        Optional<AniListMedia> animeContent = fetchAnimeContent(ids.aniListId());
        Optional<TmdbVideoContent> mainstreamContent = fetchMainstreamContent(ids, animeContent);

        if(mainstreamContent.isPresent() && animeContent.isEmpty()){
            animeContent = attemptAnimeLookupFromMainstream(mainstreamContent.get());
        }
        if(!mainstreamAnimeIntegrityValidator.dataIntegrityCheck(mainstreamContent.orElse(null), animeContent.orElse(null))){
            throw new IllegalArgumentException(
                    "AniList and TMDB content mismatch. Anime content: " + animeContent +
                            " Mainstream content: " + mainstreamContent
            );
        }

        return new VideoContentCompilation(
                databaseService.getVideoContentDb(input).orElse(null),
                animeContent.orElse(null),
                mainstreamContent.orElse(null)
        );
    }

    private IdResolutionResult resolveIds(VideoContentIdInput input){
        // Get external services ids from input
        Integer aniListId = input.aniListId();
        Integer tmdbId = input.tmdbVideoContentIdInput() != null ? input.tmdbVideoContentIdInput().tmdbId() : null;
        VideoContentFormat format = input.tmdbVideoContentIdInput() != null ? input.tmdbVideoContentIdInput().format() : VideoContentFormat.UNKNOWN;

        // Get video content from database and get available info (external ids)
        Optional<VideoContent> videoContentDb = getExistingVideoContent(input);
        if(videoContentDb.isPresent()){
            VideoContent optionalVideoContent = videoContentDb.get();
            aniListId =  isNotNullAndPositive(optionalVideoContent.getAniListId()) ? optionalVideoContent.getAniListId() : aniListId;
            tmdbId = isNotNullAndPositive(optionalVideoContent.getTmdbId()) ? optionalVideoContent.getTmdbId() : tmdbId;
            format = videoContentFormatMapper.fromVideoContentType(optionalVideoContent.getType());
        }

        return new IdResolutionResult(aniListId, tmdbId, format);
    }

    private Optional<AniListMedia> fetchAnimeContent(Integer aniListId) throws TooManyAnimeRequestsException {
        try {
            return aniListId != null ? animeService.fetchAnimeContentById(aniListId) : Optional.empty();
        } catch (TooManyAnimeRequestsException e) {
            log.error("External anime service rate limit exceeded for Id: {}", aniListId, e);
            throw e;
        }
    }

    private Optional<TmdbVideoContent> fetchMainstreamContent(IdResolutionResult ids, Optional<AniListMedia> anime) throws ContentNotFoundException {
        if(ids.tmdbId() != null){
            return mainstreamService.fetchMainstreamContentById(new TmdbVideoContentIdInput(ids.tmdbId(), ids.format()));
        }
        return anime.flatMap(mainstreamService::fetchMainstreamContentByAnime);
    }

    private Optional<AniListMedia> attemptAnimeLookupFromMainstream(TmdbVideoContent mainstreamContent) throws TooManyAnimeRequestsException {
        if(!mainstreamService.isMainstreamContentAnime(mainstreamContent)){
            return Optional.empty();
        }

        try {
            Optional<AniListMedia> anime = animeService.fetchAnimeContentByTitle(mainstreamContent.getTitle());

            if(anime.isPresent() && animeService.isConflictingContent(anime.get(), mainstreamContent.getTitle())){
                log.warn("Title-based anime lookup conflict for TMDB ID: {}", mainstreamContent.getTmdbId());
                return Optional.empty();
            }
            return anime;
        } catch (TooManyAnimeRequestsException e) {
            log.error("Anime service rate limit exceeded during title lookup for mainstream content: {}", mainstreamContent.getTmdbId(), e);
            throw e;
        }
    }

    /**
     * @param updatedVideoContent video content with new data
     * @return updated video content if it existed before in database, or <code>Optional.null()</code> otherwise
     * @see DatabaseService#updateContent(Object)
     */
    public Optional<VideoContent> updateContent(VideoContent updatedVideoContent) {
        return databaseService.updateContent(updatedVideoContent).getData();
    }

    private record IdResolutionResult(
            Integer aniListId,
            Integer tmdbId,
            VideoContentFormat format
    ){}
}