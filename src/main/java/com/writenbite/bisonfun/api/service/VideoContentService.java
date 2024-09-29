package com.writenbite.bisonfun.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.NoAccessException;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.database.entity.UserVideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import com.writenbite.bisonfun.api.database.repository.VideoContentRepository;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import com.writenbite.bisonfun.api.types.videocontent.input.TmdbIdInput;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.output.BasicInfoConnection;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import com.writenbite.bisonfun.api.types.mapper.VideoContentBasicInfoMapper;
import com.writenbite.bisonfun.api.types.mapper.VideoContentExternalInfoMapper;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.TvSeries;
import info.movito.themoviedbapi.model.core.TvSeriesResultsPage;
import info.movito.themoviedbapi.model.keywords.Keyword;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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

    @Value("${bisonfun.data-update.range-in-days}")
    private Integer dayRange;

    private final VideoContentCompilationMapper videoContentCompilationMapper;
    private final VideoContentFormatMapper videoContentFormatMapper;
    private final VideoContentRepository videoContentRepository;
    private final TmdbClient tmdbClient;
    private final AniListClient aniListClient;
    private final VideoContentMapper videoContentMapper;
    private final VideoContentBasicInfoMapper basicInfoMapper;
    private final VideoContentExternalInfoMapper externalInfoMapper;

    @Autowired
    public VideoContentService(VideoContentRepository videoContentRepository, TmdbClient tmdbClient, AniListClient aniListClient, VideoContentMapper videoContentMapper, VideoContentBasicInfoMapper basicInfoMapper, VideoContentExternalInfoMapper externalInfoMapper,
                               VideoContentFormatMapper videoContentFormatMapper,
                               VideoContentCompilationMapper videoContentCompilationMapper) {
        this.videoContentRepository = videoContentRepository;
        this.tmdbClient = tmdbClient;
        this.aniListClient = aniListClient;
        this.videoContentMapper = videoContentMapper;
        this.basicInfoMapper = basicInfoMapper;
        this.externalInfoMapper = externalInfoMapper;
        this.videoContentFormatMapper = videoContentFormatMapper;
        this.videoContentCompilationMapper = videoContentCompilationMapper;
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
                if (formats.contains(VideoContentFormat.MOVIE)) {
                    MovieResultsPage movies;
                    movies = tmdbClient.parseMovieList(query, page);
                    totalPages = Math.max(totalPages, movies.getTotalPages());
                    totalResults += movies.getTotalResults();

                    Map<Movie, VideoContent> movieMediaVideoContentMap = getMovieVideoContentMap(movies.getResults());
                    searchResult.addAll(movieMediaVideoContentMap.keySet()
                            .stream()
                            .map(movie -> basicInfoMapper.fromBasicModels(movieMediaVideoContentMap.getOrDefault(movie, null), movie, null, null))
                            .toList());
                }
                if (formats.contains(VideoContentFormat.TV)) {
                    TvSeriesResultsPage tvs;
                    tvs = tmdbClient.parseTVList(query, page);
                    totalPages = Math.max(totalPages, tvs.getTotalPages());
                    totalResults += tvs.getTotalResults();

                    Map<TvSeries, VideoContent> tvMediaVideoContentMap = getTvSeriesVideoContentMap(tvs.getResults());
                    searchResult.addAll(tvMediaVideoContentMap.keySet()
                            .stream()
                            .map(tvSeries -> basicInfoMapper.fromBasicModels(tvMediaVideoContentMap.getOrDefault(tvSeries, null), null, tvSeries, null))
                            .toList());
                }
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
                                .map(media -> basicInfoMapper.fromModels(aniListMediaVideoContentMap.getOrDefault(media, null), media, null, null))
                                .toList()
                );
            }
        }
        PageInfo pageInfo = new PageInfo(totalResults, perPage, page, totalPages, hasNextPage);
        return new BasicInfoConnection(searchResult, pageInfo);
    }


    /**
     * @param id the original video content id
     * @return video content if exist
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public Optional<VideoContent> getVideoContentById(Long id) {
        return videoContentRepository.findById(id);
    }

    /**
     * @param id the original video content id
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content
     * @throws ContentNotFoundException if content wasn't found
     * @throws ExternalInfoException when accessing to external info is causing trouble
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentById(Long id, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        Optional<VideoContent> optionalVideoContent = getVideoContentById(id);
        if (optionalVideoContent.isPresent()) {
            com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo basicInfo;
            com.writenbite.bisonfun.api.types.videocontent.VideoContent.ExternalInfo externalInfo = null;
            VideoContent videoContentDb = optionalVideoContent.get();
            if (hasExternalInfo) {
                AniListMedia anime = null;
                MovieDb movieDb = null;
                TvSeriesDb tvSeriesDb = null;

                try {
                    anime = videoContentDb.getCategory() == VideoContentCategory.ANIME ? aniListClient.parseAnimeById(videoContentDb.getAniListId()) : null;
                } catch (ContentNotFoundException | TooManyAnimeRequestsException e) {
                    log.error(e.getMessage());
                }
                movieDb = videoContentDb.getType() == MOVIE ? tmdbClient.parseMovieById(videoContentDb.getTmdbId()) : null;
                tvSeriesDb = videoContentDb.getType() == TV ? tmdbClient.parseShowById(videoContentDb.getTmdbId()) : null;
                basicInfo = basicInfoMapper.fromModels(videoContentDb, anime, movieDb, tvSeriesDb);
                if (anime != null || movieDb != null || tvSeriesDb != null) {
                    externalInfo = anime != null ? externalInfoMapper.fromAniListMedia(anime) : (movieDb != null ? externalInfoMapper.fromMovieDb(movieDb) : externalInfoMapper.fromTvSeriesDb(tvSeriesDb));
                } else {
                    throw new ExternalInfoException(basicInfo);
                }
            } else {
                basicInfo = basicInfoMapper.fromVideoContentDb(optionalVideoContent.get());
            }
            return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfo, externalInfo);
        } else {
            throw new ContentNotFoundException("Can't find VideoContent by id " + id);
        }
    }

    /**
     * @param aniListId the id from anilist.co
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content
     * @throws ContentNotFoundException if content wasn't found
     * @throws ExternalInfoException when accessing to external info is causing trouble
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByAniListId(Integer aniListId, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        Optional<VideoContent> optionalVideoContent = videoContentRepository.findByAniListId(aniListId);
        com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo basicInfo;
        com.writenbite.bisonfun.api.types.videocontent.VideoContent.ExternalInfo externalInfo = null;

        if (optionalVideoContent.isEmpty() || hasExternalInfo) {
            Exception thrownException = null;
            VideoContent videoContentDb = optionalVideoContent.orElse(null);

            MovieDb movieDb = null;
            TvSeriesDb tvSeriesDb = null;
            if (optionalVideoContent.isPresent()) {
                movieDb = videoContentDb.getType() == MOVIE ? tmdbClient.parseMovieById(videoContentDb.getTmdbId()) : null;
                tvSeriesDb = videoContentDb.getType() == TV ? tmdbClient.parseShowById(videoContentDb.getTmdbId()) : null;
            }
            AniListMedia anime = null;
            try {
                anime = aniListClient.parseAnimeById(aniListId);
            } catch (TooManyAnimeRequestsException e) {
                log.error(e.getMessage());
                if (optionalVideoContent.isEmpty()) {
                    throw new ContentNotFoundException();
                }
                thrownException = e;
            }
            basicInfo = basicInfoMapper.fromModels(videoContentDb, anime, movieDb, tvSeriesDb);
            if (thrownException != null) {
                throw new ExternalInfoException(thrownException.getMessage(), basicInfo);
            }
            externalInfo = externalInfoMapper.fromAniListMedia(anime);
        } else {
            basicInfo = basicInfoMapper.fromVideoContentDb(optionalVideoContent.get());
        }
        return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfo, externalInfo);
    }

    /**
     * @param tmdbId the id from themoviedb.com
     * @param format of a video content request
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content
     * @throws ContentNotFoundException if content wasn't found
     * @throws ExternalInfoException when accessing to external info is causing trouble
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByTmdbId(Integer tmdbId, VideoContentFormat format, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        return getVideoContentByTmdbId(tmdbId, videoContentFormatMapper.toVideoContentType(format), hasExternalInfo);
    }

    /**
     * @param tmdbId the id from themoviedb.com
     * @param type of video content from database
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content
     * @throws ContentNotFoundException if content wasn't found
     * @throws ExternalInfoException when accessing to external info is causing trouble
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByTmdbId(Integer tmdbId, VideoContentType type, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {

        Optional<VideoContent> optionalVideoContentDb = videoContentRepository.findByTmdbIdAndType(tmdbId, type);
        com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo basicInfo;
        com.writenbite.bisonfun.api.types.videocontent.VideoContent.ExternalInfo externalInfo = null;

        if (optionalVideoContentDb.isEmpty() || hasExternalInfo) {
            VideoContent videoContentDb = optionalVideoContentDb.orElse(null);
            Exception exception = null;

            AniListMedia anime = null;
            MovieDb movieDb = null;
            TvSeriesDb tvSeriesDb = null;
            List<Keyword> keywordResults = new ArrayList<>();
            String title = "";
            if (type == MOVIE) {
                movieDb = tmdbClient.parseMovieById(tmdbId);
                keywordResults = movieDb.getKeywords().getKeywords();
                title = movieDb.getTitle();
            } else if (type == TV) {
                tvSeriesDb = tmdbClient.parseShowById(tmdbId);
                keywordResults = tvSeriesDb.getKeywords().getResults();
                title = tvSeriesDb.getName();
            }
            if (videoContentDb != null && videoContentDb.getAniListId() != null) {
                try {
                    anime = aniListClient.parseAnimeById(videoContentDb.getAniListId());
                } catch (ContentNotFoundException e) {
                    log.error("Anime not found by id: " + videoContentDb.getAniListId());
                } catch (TooManyAnimeRequestsException e) {
                    log.error("It's too many requests for AniList");
                    exception = e;
                }
            } else if (!keywordResults.isEmpty()) {
                boolean isAnime = keywordResults.stream()
                        .anyMatch(keyword -> keyword.getName().equalsIgnoreCase("anime"));
                try {
                    anime = isAnime ? aniListClient.parseAnimeByName(title) : null;
                } catch (ContentNotFoundException e) {
                    log.error("Anime not found by name: " + title);
                } catch (TooManyAnimeRequestsException e) {
                    log.error("It's too many requests for AniList");
                    exception = e;
                }
            }
            basicInfo = basicInfoMapper.fromModels(videoContentDb, anime, movieDb, tvSeriesDb);

            if (exception != null) {
                throw new ExternalInfoException(exception.getMessage(), basicInfo);
            }

            externalInfo = type == MOVIE ? externalInfoMapper.fromMovieDb(movieDb) : externalInfoMapper.fromTvSeriesDb(tvSeriesDb);
        } else {
            basicInfo = basicInfoMapper.fromVideoContentDb(optionalVideoContentDb.get());
        }
        return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfo, externalInfo);
    }

    /**
     * @param imdbId the id from imdb.com
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content
     * @throws ContentNotFoundException if content wasn't found
     * @throws ExternalInfoException when accessing to external info is causing trouble
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByImdbId(String imdbId, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        Optional<VideoContent> optionalVideoContent = videoContentRepository.findByImdbId(imdbId);

        if (optionalVideoContent.isPresent()) {
            VideoContent videoContent = optionalVideoContent.get();
            if (hasExternalInfo) {
                return videoContent.getCategory() == VideoContentCategory.ANIME ? getVideoContentByAniListId(videoContent.getAniListId(), true) : getVideoContentByTmdbId(videoContent.getTmdbId(), videoContent.getType(), true);
            } else {
                return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfoMapper.fromVideoContentDb(videoContent), null);
            }
        } else {
            throw new ContentNotFoundException();
        }
    }

    /**
     * @param malId the id from myanimelist.com
     * @param hasExternalInfo if request contains need of information from external sources
     * @return video content
     * @throws ContentNotFoundException if content wasn't found
     * @throws ExternalInfoException when accessing to external info is causing trouble
     * @deprecated use {@link #getVideoContentByIdInput(VideoContentIdInput, boolean)}
     */
    @Deprecated
    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByMalId(Integer malId, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        Optional<VideoContent> optionalVideoContent = videoContentRepository.findByMalId(malId);
        if (optionalVideoContent.isPresent()) {
            VideoContent videoContent = optionalVideoContent.get();
            if (hasExternalInfo) {
                return getVideoContentByAniListId(videoContent.getAniListId(), true);
            } else {
                return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfoMapper.fromVideoContentDb(videoContent), null);
            }
        } else {
            throw new ContentNotFoundException();
        }
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
        basicInfo = basicInfoMapper.fromVideoContentDb(getOrCreateVideoContent(input));
        if (hasExternalInfo){
            VideoContentCompilation compilation;
            try {
                compilation = fetchVideoContent(input);
            } catch (TooManyAnimeRequestsException e) {
                log.error(e.getMessage());
                throw new ExternalInfoException(e.getMessage(), basicInfo);
            }
            if (compilation.aniListMedia() != null) {
                externalInfo = externalInfoMapper.fromAniListMedia(compilation.aniListMedia());
            } else if (compilation.movieDb() != null) {
                externalInfo = externalInfoMapper.fromMovieDb(compilation.movieDb());
            } else if (compilation.tvSeriesDb() != null) {
                externalInfo = externalInfoMapper.fromTvSeriesDb(compilation.tvSeriesDb());
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

        VideoContent videoContent = videoContentMapper.fromModels(fetchedContent.aniListMedia(), fetchedContent.movieDb(), fetchedContent.tvSeriesDb());
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
        VideoContent videoContent = videoContentMapper.fromModels(fetchedContent.aniListMedia(), fetchedContent.movieDb(), fetchedContent.tvSeriesDb());
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
        if (videoContent.getType() == MOVIE) {
            MovieDb movie = tmdbClient.parseTmdbMovieByName(videoContent.getTitle(), videoContent.getYear());
            if(isConflictingContent(videoContent.getTitle(), movie.getTitle())) {
               throw new ContentNotFoundException();
            }
            tmdbId = movie.getId();
        } else if (videoContent.getType() == TV) {
            TvSeriesDb tv = tmdbClient.parseTmdbTvByName(videoContent.getTitle(), videoContent.getYear());
            if(isConflictingContent(videoContent.getTitle(), tv.getName()) || isConflictingContent(videoContent.getTitle(), tv.getOriginalName())){
               throw new ContentNotFoundException();
            }
            tmdbId = tv.getId();
        }
        videoContent.setTmdbId(tmdbId);
    }

    /**
     * @param videoContent video content from database which need imdb id to be checked
     * @throws ContentNotFoundException proper content couldn't be found on themoviedb
     * @throws JsonProcessingException when response from themoviedb couldn't be properly mapped
     * @see #isConflictingContent(String, String)
     */
    private void updateImdbId(VideoContent videoContent) throws JsonProcessingException, ContentNotFoundException {
        String imdbId = IMDB_ID_DEFAULT;
        if (videoContent.getType() == MOVIE) {
            MovieDb movie = isNotNullAndPositive(videoContent.getTmdbId()) ? tmdbClient.parseMovieById(videoContent.getTmdbId()) : tmdbClient.parseTmdbMovieByName(videoContent.getTitle(), videoContent.getYear());
            if (isConflictingContent(videoContent.getTitle(), movie.getTitle())) {
                throw new ContentNotFoundException();
            }
            imdbId = movie.getImdbID() != null ? movie.getImdbID() : imdbId;
        } else if (videoContent.getType() == TV) {
            TvSeriesDb tv = isNotNullAndPositive(videoContent.getTmdbId()) ? tmdbClient.parseShowById(videoContent.getTmdbId()) : tmdbClient.parseTmdbTvByName(videoContent.getTitle(), videoContent.getYear());
            if(isConflictingContent(videoContent.getTitle(), tv.getName()) || isConflictingContent(videoContent.getTitle(), tv.getOriginalName())){
                throw new ContentNotFoundException();
            }
            imdbId = tv.getExternalIds() != null ? tv.getExternalIds().getImdbId() : null;
        }
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
     * @see #isTmdbContentAnime(MovieDb, TvSeriesDb)
     * @see #isConflictingContent(AniListMedia, String)
     */
    public VideoContentCompilation fetchVideoContent(VideoContentIdInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        VideoContent videoContentDb = null;
        AniListMedia anime = null;
        Pair<MovieDb, TvSeriesDb> tmdbContent = Pair.of(null, null);
        MovieDb movieDb;
        TvSeriesDb tvSeriesDb;
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
            tmdbContent = fetchTmdbContent(new TmdbIdInput(tmdbId, format));
        } else if (anime != null) {
            tmdbContent = fetchTmdbContentFromAnime(anime);
        }
        movieDb = tmdbContent.getLeft();
        tvSeriesDb = tmdbContent.getRight();

        if (isTmdbContentAnime(movieDb, tvSeriesDb)) {
            String tmdbTitle = movieDb != null ? movieDb.getTitle() : tvSeriesDb.getName();
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

        return new VideoContentCompilation(videoContentDb, anime, movieDb, tvSeriesDb);
    }

    /**
     * @param tmdbIdInput record class which contains id of themoviedb and type of content
     * @return tmdb content pair of movie and tv series
     */
    private Pair<MovieDb, TvSeriesDb> fetchTmdbContent(TmdbIdInput tmdbIdInput) throws ContentNotFoundException {
        MovieDb movie = null;
        TvSeriesDb tv = null;
        if(tmdbIdInput.format() == VideoContentFormat.MOVIE){
            movie = tmdbClient.parseMovieById(tmdbIdInput.tmdbId());
        } else if (tmdbIdInput.format() == VideoContentFormat.TV) {
            tv = tmdbClient.parseShowById(tmdbIdInput.tmdbId());
        }
        return Pair.of(movie, tv);
    }

    /**
     * @param anime record class response from anilist
     * @return tmdb content pair of movie and tv series
     */
    private Pair<MovieDb, TvSeriesDb> fetchTmdbContentFromAnime(AniListMedia anime){
        MovieDb movie = null;
        TvSeriesDb tv = null;
        try {
            VideoContentType videoContentType = videoContentMapper.animeType(anime.format());
            if (videoContentType == MOVIE) {
                movie = tmdbClient.parseTmdbMovieByName(anime.title().english(), anime.startDate().year());
            } else if (videoContentType == TV) {
                tv = tmdbClient.parseTmdbTvByName(anime.title().english(), anime.startDate().year());
            }
        } catch (JsonProcessingException | ContentNotFoundException e) {
            log.error(e.getMessage());
        }
        return Pair.of(movie, tv);
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
     * @param movieDb response class from themoviedb about movies
     * @param tvSeriesDb response class from themoviedb about tv shows
     * @return <code>true</code> if one of the tmdb content exist and have 'Anime' keyword and <code>false</code> otherwise
     * @see #animeKeywordCheck(List)
     */
    private boolean isTmdbContentAnime(MovieDb movieDb, TvSeriesDb tvSeriesDb){
        return ((movieDb != null && movieDb.getKeywords() != null && animeKeywordCheck(movieDb.getKeywords().getKeywords())) || (tvSeriesDb != null && tvSeriesDb.getKeywords() != null && animeKeywordCheck(tvSeriesDb.getKeywords().getResults())));
    }

    /**
     * @param keywords list of keywords from tmdb content
     * @return <code>true</code> if list of keywords contains 'anime' keyword
     */
    private boolean animeKeywordCheck(List<Keyword> keywords){
        if (!keywords.isEmpty()) {
            return keywords.stream().anyMatch(keyword -> "anime".equalsIgnoreCase(keyword.getName()));
        }
        return false;
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

    private Map<Movie, VideoContent> getMovieVideoContentMap(List<Movie> movies) {
        Map<Integer, Movie> movieIds = movies.stream()
                .collect(Collectors.toMap(
                        Movie::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        List<VideoContent> movieVideoContent = videoContentRepository.findListByTmdbIdsAndType(movieIds.keySet(), MOVIE);
        Map<Integer, VideoContent> movieMap = movieVideoContent.stream()
                .collect(
                        Collectors.toMap(
                                VideoContent::getTmdbId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        )
                );
        Map<Movie, VideoContent> movieVideoContentMap = new HashMap<>();
        for (Integer tmdbId : movieIds.keySet()) {
            movieVideoContentMap.put(movieIds.get(tmdbId), movieMap.getOrDefault(tmdbId, null));
        }
        return movieVideoContentMap;
    }

    private Map<TvSeries, VideoContent> getTvSeriesVideoContentMap(List<TvSeries> tvs) {
        Map<Integer, TvSeries> tvIds = tvs.stream()
                .collect(Collectors.toMap(
                        TvSeries::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        List<VideoContent> tvVideoContent = videoContentRepository.findListByTmdbIdsAndType(tvIds.keySet(), TV);
        Map<Integer, VideoContent> tvMap = tvVideoContent.stream()
                .collect(
                        Collectors.toMap(
                                VideoContent::getTmdbId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        )
                );
        Map<TvSeries, VideoContent> tvVideoContentMap = new HashMap<>();
        for (Integer tmdbId : tvIds.keySet()) {
            tvVideoContentMap.put(tvIds.get(tmdbId), tvMap.getOrDefault(tmdbId, null));
        }
        return tvVideoContentMap;
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
                .map(media -> basicInfoMapper.fromModels(aniListMediaVideoContentMap.getOrDefault(media, null), media, null, null))
                .collect(Collectors.toList());
    }

    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getMovieTrends() throws ContentNotFoundException {
        MovieResultsPage movieTrends = null;
        try {
            movieTrends = tmdbClient.parseMovieTrends();
        } catch (NoAccessException e) {
            log.error(e.getMessage());
            throw new ContentNotFoundException();
        }
        Map<Movie, VideoContent> movieMediaVideoContentMap = getMovieVideoContentMap(movieTrends.getResults());
        return movieMediaVideoContentMap.keySet()
                .stream()
                .map(movie -> basicInfoMapper.fromBasicModels(movieMediaVideoContentMap.getOrDefault(movie, null), movie, null, null))
                .collect(Collectors.toList());
    }

    public List<com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo> getTvTrends() throws ContentNotFoundException {
        TvSeriesResultsPage tvTrends;
        try {
            tvTrends = tmdbClient.parseTVTrends();
        } catch (NoAccessException e) {
            log.error(e.getMessage());
            throw new ContentNotFoundException();
        }
        Map<TvSeries, VideoContent> tvMediaVideoContentMap = getTvSeriesVideoContentMap(tvTrends.getResults());
        return tvMediaVideoContentMap.keySet()
                .stream()
                .map(tvSeries -> basicInfoMapper.fromBasicModels(tvMediaVideoContentMap.getOrDefault(tvSeries, null), null, tvSeries, null))
                .collect(Collectors.toList());
    }
}
