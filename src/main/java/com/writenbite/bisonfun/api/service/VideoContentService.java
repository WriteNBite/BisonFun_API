package com.writenbite.bisonfun.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaMapper;
import com.writenbite.bisonfun.api.client.anilist.mapper.AniListMediaTitleMapper;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.tmdb.TmdbAnimeChecker;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.client.tmdb.mapper.TmdbVideoContentMapper;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContentResultsPage;
import com.writenbite.bisonfun.api.database.entity.UserVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.database.mapper.VideoContentTypeMapper;
import com.writenbite.bisonfun.api.database.repository.VideoContentRepository;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import com.writenbite.bisonfun.api.types.videocontent.input.TmdbIdInput;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.output.BasicInfoConnection;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.writenbite.bisonfun.api.database.entity.VideoContentType.*;

@Slf4j
@Service
public class VideoContentService {
    private final static int TMDB_ID_DEFAULT = -1;
    private final static int ANILIST_ID_DEFAULT = -1;
    private final static String IMDB_ID_DEFAULT = "";
    private final static int MAL_ID_DEFAULT = -1;
    private final AniListMediaMapper aniListMediaMapper;
    private final TmdbVideoContentMapper tmdbVideoContentMapper;

    @Value("${bisonfun.data-update.range-in-days}")
    private Integer dayRange;

    private final VideoContentCompilationMapper videoContentCompilationMapper;
    private final VideoContentFormatMapper videoContentFormatMapper;
    private final VideoContentRepository videoContentRepository;
    private final TmdbClient tmdbClient;
    private final AniListClient aniListClient;
    private final AniListMediaTitleMapper aniListMediaTitleMapper;
    private final VideoContentMapper videoContentMapper;
    private final RawVideoContentFactory rawVideoContentFactory;
    private final VideoContentTypeMapper videoContentTypeMapper;

    @Autowired
    public VideoContentService(VideoContentRepository videoContentRepository, TmdbClient tmdbClient, AniListClient aniListClient, VideoContentMapper videoContentMapper, AniListMediaTitleMapper aniListMediaTitleMapper,
                               VideoContentFormatMapper videoContentFormatMapper,
                               VideoContentCompilationMapper videoContentCompilationMapper, RawVideoContentFactory rawVideoContentFactory, VideoContentTypeMapper videoContentTypeMapper, AniListMediaMapper aniListMediaMapper, TmdbVideoContentMapper tmdbVideoContentMapper) {
        this.videoContentRepository = videoContentRepository;
        this.tmdbClient = tmdbClient;
        this.aniListClient = aniListClient;
        this.videoContentMapper = videoContentMapper;
        this.aniListMediaTitleMapper = aniListMediaTitleMapper;
        this.videoContentFormatMapper = videoContentFormatMapper;
        this.videoContentCompilationMapper = videoContentCompilationMapper;
        this.rawVideoContentFactory = rawVideoContentFactory;
        this.videoContentTypeMapper = videoContentTypeMapper;
        this.aniListMediaMapper = aniListMediaMapper;
        this.tmdbVideoContentMapper = tmdbVideoContentMapper;
    }

    public Connection<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> search(
            String query,
            com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory category,
            List<VideoContentFormat> formats,
            Integer page
    ) throws ContentNotFoundException {
        int totalPages = 0;
        int totalResults = 0;
        int perPage = 0;
        boolean hasNextPage = false;

        List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> searchResult = new ArrayList<>();
        switch (category) {
            case MAINSTREAM -> {
                List<TmdbVideoContentResultsPage> searchResults = new ArrayList<>();
                if (formats.contains(VideoContentFormat.MOVIE)) {
                    searchResults.add(tmdbClient.parseMovieList(query, page));
                }
                if (formats.contains(VideoContentFormat.TV)) {
                    searchResults.add(tmdbClient.parseTVList(query, page));
                }
                totalPages = Math.max(totalPages, searchResults.stream().mapToInt(TmdbVideoContentResultsPage::totalPages).max().orElse(0));
                totalResults += searchResults.stream().mapToInt(TmdbVideoContentResultsPage::totalResults).sum();

                Map<TmdbSimpleVideoContent, Optional<VideoContent>> movieMediaVideoContentMap = getTmdbVideoContentMap(searchResults.stream().flatMap(result -> result.results().stream()).toList());
                searchResult.addAll(movieMediaVideoContentMap.keySet()
                        .stream()
                        .map(movie -> rawVideoContentFactory.toBasicInfo(movie, movieMediaVideoContentMap.getOrDefault(movie, null).orElse(null)))
                        .toList());
                perPage = searchResult.size();
                hasNextPage = page < totalPages;
            }
            case ANIME -> {
                Collection<AniListMediaFormat> mediaFormats = videoContentFormatMapper.toAniListMediaFormat(formats);
                AniListPage<AniListMedia> aniListResults;
                try {
                    aniListResults = aniListClient.parse(query, page, mediaFormats);
                } catch (TooManyAnimeRequestsException e) {
                    log.error(e.getMessage());
                    throw new ContentNotFoundException();
                }
                totalPages = aniListResults.getPageInfo().lastPage();
                perPage = aniListResults.getPageInfo().perPage();
                totalResults = totalPages * perPage;
                hasNextPage = aniListResults.getPageInfo().hasNextPage();

                Map<AniListMedia, VideoContent> aniListMediaVideoContentMap = getAniListMediaVideoContentMap(aniListResults.getList());
                searchResult.addAll(
                        aniListMediaVideoContentMap.keySet()
                                .stream()
                                .map(media -> rawVideoContentFactory.toBasicInfo(aniListMediaVideoContentMap.getOrDefault(media, null), media))
                                .toList()
                );
            }
        }
        PageInfo pageInfo = new PageInfo(totalResults, perPage, page, totalPages, hasNextPage);
        return new BasicInfoConnection(searchResult, pageInfo);
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
     * @see #validateInput(VideoContentIdInput)
     * @see #getExistingVideoContent(VideoContentIdInput)
     * @see #isRequireUpdate(VideoContent)
     * @see #fetchAndUpdateVideoContent(VideoContentIdInput)
     * @see #fetchAndSaveVideoContent(VideoContentIdInput)
     */
    public VideoContent getOrCreateVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        validateInput(input);

        Optional<VideoContent> optionalValue = getExistingVideoContent(input);
        if(optionalValue.isPresent()){
            VideoContent videoContent = optionalValue.get();
            if(isRequireUpdate(videoContent)){
                return fetchAndUpdateVideoContent(input);
            }
            return videoContent;
        }else{
            return fetchAndSaveVideoContent(input);
        }
    }

    /**
     * @param videoContent video content which need to check
     * @return <code>true</code> if video content was last updated more than {@link #dayRange} days ago and <code>false</code> otherwise
     */
    private boolean isRequireUpdate(VideoContent videoContent){
        return LocalDate.now().minusDays(dayRange).isAfter(videoContent.getLastUpdated());
    }

    /**
     * Check input value if it contains one of the ids
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @see #isNotNullAndPositive(Number)
     */
    private void validateInput(VideoContentIdInput input){
        if (input == null || !(isNotNullAndPositive(input.videoContentId()) || input.tmdbIdInput() != null || isNotNullAndPositive(input.aniListId()))) {
            IllegalArgumentException exception = new IllegalArgumentException("Each input is null");
            log.error(exception.getMessage());
            throw exception;
        }
    }

    /**
     * Return existing video content and update video content in case, if it contains unchecked ids (anilist, mal, themoviedb, imdb)
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return {@link Optional} with {@link VideoContent} if it exists in the database and <code>Optional.empty()</code> otherwise
     * @see #getVideoContentDb(VideoContentIdInput)
     * @see #checkAndUpdateExternalIds(VideoContent)
     */
    private Optional<VideoContent> getExistingVideoContent(VideoContentIdInput input){
        Optional<VideoContent> optionalVideoContent = getVideoContentDb(input);
        if (optionalVideoContent.isPresent()) {
            VideoContent videoContent = optionalVideoContent.get();
            //Check video content ids
            if (checkAndUpdateExternalIds(videoContent)) {
                saveVideoContent(videoContent);
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
     * @see #checkVideoContentExistence(VideoContentIdInput)
     * @see #getOrCreateVideoContent(VideoContentIdInput)
     */
    private VideoContent fetchAndSaveVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        VideoContentCompilation fetchedContent = fetchVideoContent(input);
        VideoContentIdInput fetchedInput = videoContentCompilationMapper.fromCompilation(fetchedContent);

        if(checkVideoContentExistence(fetchedInput)){
            return getOrCreateVideoContent(fetchedInput);
        }

        VideoContent videoContent = rawVideoContentFactory.toVideoContentDb(fetchedContent.aniListMedia(), fetchedContent.tmdbVideoContent());
        return saveVideoContent(videoContent);
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
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return <code>true</code> if video content is existed in database and <code>false</code> otherwise
     */
    private boolean checkVideoContentExistence(VideoContentIdInput input){
        boolean existence = false;
        if(input.aniListId() != null && input.aniListId() > 0){
            existence = videoContentRepository.existsByAniListId(input.aniListId());
        }
        if(input.tmdbIdInput() != null && input.tmdbIdInput().tmdbId() > 0){
            existence = existence || videoContentRepository.existsByTmdbIdAndType(input.tmdbIdInput().tmdbId(), videoContentFormatMapper.toVideoContentType(input.tmdbIdInput().format()));
        }
        return existence;
    }

    /**
     * @param videoContent video content from database which need external ids (anilist, mal, themoviedb, imdb) to be checked
     * @return <code>true</code> if video content had unchecked external ids, it was updated and <code>false</code> otherwise
     * @see #checkAndUpdateTmdbAndImdbIds(VideoContent)
     * @see #checkAndUpdateAniListAndMalId(VideoContent)
     */
    public boolean checkAndUpdateExternalIds(VideoContent videoContent) {
        boolean isChanged = checkAndUpdateTmdbAndImdbIds(videoContent);

        isChanged |= checkAndUpdateAniListAndMalId(videoContent);

        return isChanged;
    }

    /**
     * @param videoContent video content from database which need external ids (themoviedb, imdb) to be checked
     * @return <code>true</code> if video content had unchecked external ids, it was updated and <code>false</code> otherwise
     * @see #updateTmdbId(VideoContent)
     * @see #updateImdbId(VideoContent)
     */
    private boolean checkAndUpdateTmdbAndImdbIds(VideoContent videoContent) {
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
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
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
     * @throws JsonProcessingException when response from themoviedb couldn't be properly mapped
     * @see #isConflictingContent(String, String)
     */
    private void updateTmdbId(VideoContent videoContent) throws ContentNotFoundException, JsonProcessingException {
        int tmdbId = TMDB_ID_DEFAULT;
        TmdbSimpleVideoContent tmdbVideoContent = null;
        if (videoContent.getType() == MOVIE) {
            tmdbVideoContent = tmdbClient.parseTmdbMovieByName(videoContent.getTitle(), videoContent.getYear());
        } else if (videoContent.getType() == TV) {
            tmdbVideoContent = tmdbClient.parseTmdbTvByName(videoContent.getTitle(), videoContent.getYear());
        }

        if(tmdbVideoContent == null || isConflictingContent(videoContent.getTitle(), tmdbVideoContent.getTitle()) || isConflictingContent(videoContent.getTitle(), tmdbVideoContent.getOriginalTitle())){
            throw new ContentNotFoundException();
        }
        tmdbId = tmdbVideoContent.getTmdbId();
        videoContent.setTmdbId(tmdbId);
    }

    /**
     * @param videoContent video content from database which need imdb id to be checked
     * @throws ContentNotFoundException proper content couldn't be found on themoviedb
     * @see #isConflictingContent(String, String)
     */
    private void updateImdbId(VideoContent videoContent) throws ContentNotFoundException {
        String imdbId = IMDB_ID_DEFAULT;
        TmdbVideoContent tmdbVideoContent = null;
        if(isNotNullAndPositive(videoContent.getTmdbId())) {
            if (videoContent.getType() == MOVIE) {
                tmdbVideoContent = tmdbClient.parseMovieById(videoContent.getTmdbId());
            } else if (videoContent.getType() == TV) {
                tmdbVideoContent = tmdbClient.parseShowById(videoContent.getTmdbId());
            }
        }
        if(tmdbVideoContent == null || isConflictingContent(videoContent.getTitle(), tmdbVideoContent.getTitle()) || isConflictingContent(videoContent.getTitle(), tmdbVideoContent.getOriginalTitle())){
            throw new ContentNotFoundException();
        }
        imdbId = tmdbVideoContent.getImdbId().orElse(null);
        videoContent.setImdbId(imdbId);
    }

    /**
     * @param videoContent video content from database which need external ids (anilist, mal) to be checked
     * @return <code>true</code> if video content had unchecked external ids, it was updated and <code>false</code> otherwise
     * @see #updateAniListId(VideoContent)
     * @see #updateMalId(VideoContent)
     */
    private boolean checkAndUpdateAniListAndMalId(VideoContent videoContent) {
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

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return {@link VideoContentCompilation} record class which contains fetched video content (original from database, anilist and themoviedb)
     * @throws ContentNotFoundException proper content couldn't be found everywhere
     * @throws TooManyAnimeRequestsException when service reached rate limit set in anilist
     * @see #getExistingVideoContent(VideoContentIdInput)
     * @see #isNotNullAndPositive(Number)
     * @see #fetchTmdbContent(TmdbIdInput)
     * @see #fetchTmdbContentFromAnime(AniListMedia)
     * @see com.writenbite.bisonfun.api.client.tmdb.TmdbAnimeChecker#isTmdbContentAnime(TmdbVideoContent)
     * @see #isConflictingContent(AniListMedia, String)
     */
    public VideoContentCompilation fetchVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        VideoContent videoContentDb = null;
        AniListMedia anime = null;
        TmdbVideoContent tmdbContent = null;
        boolean isContentAnime = false;
        Integer aniListId = input.aniListId();
        Integer tmdbId = input.tmdbIdInput() != null ? input.tmdbIdInput().tmdbId() : null;
        VideoContentFormat format = input.tmdbIdInput() != null ? input.tmdbIdInput().format() : VideoContentFormat.UNKNOWN;

        Optional<VideoContent> optionalVideoContent = getExistingVideoContent(input);
        if(optionalVideoContent.isPresent()){
            videoContentDb = optionalVideoContent.get();
            aniListId =  isNotNullAndPositive(videoContentDb.getAniListId()) ? videoContentDb.getAniListId() : aniListId;
            tmdbId = isNotNullAndPositive(videoContentDb.getTmdbId()) ? videoContentDb.getTmdbId() : tmdbId;
            format = videoContentFormatMapper.fromVideoContentType(videoContentDb.getType());
        }

        if (aniListId != null) {
            anime = aniListClient.parseAnimeById(input.aniListId());
        }

        if (tmdbId != null) {
            tmdbContent = fetchTmdbContent(new TmdbIdInput(tmdbId, format)).orElse(null);
        } else if (anime != null) {
            tmdbContent = fetchTmdbContentFromAnime(anime).orElse(null);
        }

        if (TmdbAnimeChecker.isTmdbContentAnime(tmdbContent)) {
            String tmdbTitle = tmdbContent.getTitle();
            if (anime == null) {
                try {
                    anime = aniListClient.parseAnimeByName(tmdbTitle);
                } catch (ContentNotFoundException e) {
                    log.error(e.getMessage());
                }
            }
            if (isConflictingContent(anime, tmdbTitle)) {
                throw new IllegalArgumentException("Id's are related to different video content");
            }
        }

        return new VideoContentCompilation(videoContentDb, anime, tmdbContent);
    }

    /**
     * @param tmdbIdInput record class which contains id of themoviedb and type of content
     * @return tmdb content pair of movie and tv series
     */
    private Optional<TmdbVideoContent> fetchTmdbContent(TmdbIdInput tmdbIdInput) throws ContentNotFoundException {
        if(tmdbIdInput.format() == VideoContentFormat.MOVIE){
            return Optional.ofNullable(tmdbClient.parseMovieById(tmdbIdInput.tmdbId()));
        } else if (tmdbIdInput.format() == VideoContentFormat.TV) {
            return Optional.ofNullable(tmdbClient.parseShowById(tmdbIdInput.tmdbId()));
        }
        return Optional.empty();
    }

    /**
     * @param anime record class response from anilist
     * @return tmdb content pair of movie and tv series
     */
    private Optional<TmdbVideoContent> fetchTmdbContentFromAnime(AniListMedia anime){
        TmdbSimpleVideoContent tmdbSimpleVideoContent = null;
        try {
            VideoContentType videoContentType = videoContentTypeMapper.fromAniListMediaFormat(anime.format());
            Integer year = anime.startDate() != null ? anime.startDate().year() : null;
            if (videoContentType == MOVIE) {
                 tmdbSimpleVideoContent = tmdbClient.parseTmdbMovieByName(
                        aniListMediaTitleMapper.animeEnglishTitle(anime.title()),
                        year
                );
            } else if (videoContentType == TV) {
                tmdbSimpleVideoContent = tmdbClient.parseTmdbTvByName(
                        aniListMediaTitleMapper.animeEnglishTitle(anime.title()),
                        year
                );
            }

            if(tmdbSimpleVideoContent != null && isNotNullAndPositive(tmdbSimpleVideoContent.getTmdbId())){
                return fetchTmdbContent(new TmdbIdInput(tmdbSimpleVideoContent.getTmdbId(), videoContentFormatMapper.fromVideoContentType(tmdbSimpleVideoContent.getVideoContentType())));
            }
        } catch (ContentNotFoundException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * @param anime record class response from anilist
     * @param title title need to check if it's part of anime's titles
     * @return <code>true</code> when there's titles aren't matching and <code>false</code> when there's no conflict
     */
    private boolean isConflictingContent(AniListMedia anime, String title) {
        if(anime != null) {
            String animeTitle = anime.title().english() != null ? anime.title().english() : (anime.title().romaji() != null ? anime.title().romaji() : anime.title().nativeTitle());
            return !(animeTitle.equalsIgnoreCase(title) || anime.synonyms().contains(title));
        }
        return false;
    }

    private boolean isConflictingContent(String originalTitle, String sideTitle){
        return !originalTitle.equalsIgnoreCase(sideTitle);
    }

    /**
     * @param input record with available ids for search (original, anilist, themoviedb)
     * @return existed video content from database or <code>Optional.null()</code> otherwise
     */
    public Optional<VideoContent> getVideoContentDb(VideoContentIdInput input) {
        if (input.videoContentId() != null) {
            return videoContentRepository.findById(input.videoContentId());
        }
        if (input.aniListId() != null) {
            return videoContentRepository.findByAniListId(input.aniListId());
        }
        if (input.tmdbIdInput() != null) {
            return videoContentRepository.findByTmdbIdAndType(input.tmdbIdInput().tmdbId(), videoContentFormatMapper.toVideoContentType(input.tmdbIdInput().format()));
        }
        return Optional.empty();
    }

    public VideoContent saveVideoContent(VideoContent videoContent) {
        return videoContentRepository.save(videoContent);
    }

    /**
     * @param updatedVideoContent video content with new data
     * @return updated video content if it existed before in database, or <code>Optional.null()</code> otherwise
     * @see #getVideoContentByVideoContent(VideoContent)
     * @see #updatePoster(VideoContent, VideoContent)
     * @see #updateCategory(VideoContent, VideoContent)
     * @see #updateType(VideoContent, VideoContent)
     * @see #updateYear(VideoContent, VideoContent)
     */
    public Optional<VideoContent> updateContent(VideoContent updatedVideoContent) {
        return getVideoContentByVideoContent(updatedVideoContent)
                .map(existingVideoContent -> {
                    updatePoster(existingVideoContent, updatedVideoContent);
                    updateCategory(existingVideoContent, updatedVideoContent);
                    updateType(existingVideoContent, updatedVideoContent);
                    updateYear(existingVideoContent, updatedVideoContent);

                    return videoContentRepository.save(existingVideoContent);
                });
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     * @see #isNonEmpty(String)
     */
    private void updatePoster(VideoContent existing, VideoContent updated) {
        if (isNonEmpty(updated.getPoster())) {
            existing.setPoster(updated.getPoster());
        }
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     */
    private void updateCategory(VideoContent existing, VideoContent updated) {
        if (existing.getCategory() == VideoContentCategory.MAINSTREAM) {
            existing.setCategory(updated.getCategory());
        }
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     */
    private void updateType(VideoContent existing, VideoContent updated) {
        if (existing.getType() == UNKNOWN) {
            existing.setType(updated.getType());
        }
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     */
    private void updateYear(VideoContent existing, VideoContent updated) {
        if (existing.getYear() <= 0) {
            existing.setYear(updated.getYear());
        }
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isNotNullAndPositive(Number number){
        return number != null && number.intValue() > 0;
    }

    public Optional<VideoContent> getVideoContentByVideoContent(@NonNull VideoContent videoContent) {
        if (videoContent.getId() != null) {
            return videoContentRepository.findById(videoContent.getId());
        } else if (videoContent.getAniListId() != null) {
            return videoContentRepository.findByAniListId(videoContent.getAniListId());
        } else if (videoContent.getTmdbId() != null) {
            return videoContentRepository.findByTmdbIdAndType(videoContent.getTmdbId(), videoContent.getType());
        }
        return Optional.empty();
    }

    public void saveVideoContentFromUserVideoContentList(List<UserVideoContent> userVideoContentList) {
        for (UserVideoContent userVideoContent : userVideoContentList) {
            VideoContent videoContent = userVideoContent.getVideoContent();
            videoContentRepository.findById(videoContent.getId()).ifPresent(dbContent -> videoContent.setId(dbContent.getId()));
            videoContentRepository.save(videoContent);
        }
    }

    private Map<AniListMedia, VideoContent> getAniListMediaVideoContentMap(List<AniListMedia> aniListMedia) {
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
        Map<AniListMedia, VideoContent> animeVideoContentMap = new HashMap<>();
        for (Integer aniListId : aniListIds.keySet()) {
            animeVideoContentMap.put(aniListIds.get(aniListId), animeMap.getOrDefault(aniListId, null));
        }
        return animeVideoContentMap;
    }

    private Map<TmdbSimpleVideoContent, Optional<VideoContent>> getTmdbVideoContentMap(List<TmdbSimpleVideoContent> tmdbList){
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

    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getAnimeTrends() throws ContentNotFoundException {
        AniListPage<AniListMedia> aniListMedia;
        try {
            aniListMedia = aniListClient
                    .parseAnimeTrends();
        } catch (TooManyAnimeRequestsException e) {
            log.error(e.getMessage());
            throw new ContentNotFoundException();
        }
        Map<AniListMedia, VideoContent> aniListMediaVideoContentMap = getAniListMediaVideoContentMap(aniListMedia.getList());
        return aniListMediaVideoContentMap.keySet()
                .stream()
                .map(media -> rawVideoContentFactory.toBasicInfo(aniListMediaVideoContentMap.getOrDefault(media, null), media))
                .collect(Collectors.toList());
    }

    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getMovieTrends() throws ContentNotFoundException {
        TmdbVideoContentResultsPage movieTrends;
        try {
            movieTrends = tmdbClient.parseMovieTrends();
        } catch (NoAccessException e) {
            log.error(e.getMessage());
            throw new ContentNotFoundException();
        }
        Map<TmdbSimpleVideoContent, Optional<VideoContent>> movieMediaVideoContentMap = getTmdbTypedVideoContentMap(movieTrends.results(), MOVIE);

        return movieMediaVideoContentMap.keySet()
                .stream()
                .map(movie -> rawVideoContentFactory.toBasicInfo(movieMediaVideoContentMap.get(movie).orElse(null), movie))
                .collect(Collectors.toList());
    }

    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getTvTrends() throws ContentNotFoundException {
        TmdbVideoContentResultsPage tvTrends;
        try {
            tvTrends = tmdbClient.parseTVTrends();
        } catch (NoAccessException e) {
            log.error(e.getMessage());
            throw new ContentNotFoundException();
        }
        Map<TmdbSimpleVideoContent, Optional<VideoContent>> tvMediaVideoContentMap = getTmdbTypedVideoContentMap(tvTrends.results(), TV);
        return tvMediaVideoContentMap.keySet()
                .stream()
                .map(tvSeries -> rawVideoContentFactory.toBasicInfo(tvMediaVideoContentMap.get(tvSeries).orElse(null), tvSeries))
                .collect(Collectors.toList());
    }
}
