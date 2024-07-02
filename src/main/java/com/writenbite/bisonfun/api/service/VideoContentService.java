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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.writenbite.bisonfun.api.database.entity.VideoContentType.*;

@Slf4j
@Service
public class VideoContentService {
    private final VideoContentFormatMapper videoContentFormatMapper;
    private final VideoContentRepository videoContentRepository;
    private final TmdbClient tmdbClient;
    private final AniListClient aniListClient;
    private final VideoContentMapper videoContentMapper;
    private final VideoContentBasicInfoMapper basicInfoMapper;
    private final VideoContentExternalInfoMapper externalInfoMapper;

    @Autowired
    public VideoContentService(VideoContentRepository videoContentRepository, TmdbClient tmdbClient, AniListClient aniListClient, VideoContentMapper videoContentMapper, VideoContentBasicInfoMapper basicInfoMapper, VideoContentExternalInfoMapper externalInfoMapper,
                               VideoContentFormatMapper videoContentFormatMapper) {
        this.videoContentRepository = videoContentRepository;
        this.tmdbClient = tmdbClient;
        this.aniListClient = aniListClient;
        this.videoContentMapper = videoContentMapper;
        this.basicInfoMapper = basicInfoMapper;
        this.externalInfoMapper = externalInfoMapper;
        this.videoContentFormatMapper = videoContentFormatMapper;
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
                    try {
                        movies = tmdbClient.parseMovieList(query, page);
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                        throw new ContentNotFoundException();
                    }
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
                    try {
                        tvs = tmdbClient.parseTVList(query, page);
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                        throw new ContentNotFoundException();
                    }
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

    public Optional<VideoContent> getVideoContentById(Long id) {
        return videoContentRepository.findById(id);
    }

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
                try {
                    movieDb = videoContentDb.getType() == MOVIE ? tmdbClient.parseMovieById(videoContentDb.getTmdbId()) : null;
                    tvSeriesDb = videoContentDb.getType() == TV ? tmdbClient.parseShowById(videoContentDb.getTmdbId()) : null;
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
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
                try {
                    movieDb = videoContentDb.getType() == MOVIE ? tmdbClient.parseMovieById(videoContentDb.getTmdbId()) : null;
                    tvSeriesDb = videoContentDb.getType() == TV ? tmdbClient.parseShowById(videoContentDb.getTmdbId()) : null;
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
            }
            AniListMedia anime = null;
            try {
                anime = aniListClient.parseAnimeById(aniListId);
            } catch (TooManyAnimeRequestsException e) {
                log.error(e.getMessage());
                if(optionalVideoContent.isEmpty()){
                    throw new ContentNotFoundException();
                }
                thrownException = e;
            }
            basicInfo = basicInfoMapper.fromModels(videoContentDb, anime, movieDb, tvSeriesDb);
            if(thrownException != null){
                throw new ExternalInfoException(thrownException.getMessage(), basicInfo);
            }
            externalInfo = externalInfoMapper.fromAniListMedia(anime);
        } else {
            basicInfo = basicInfoMapper.fromVideoContentDb(optionalVideoContent.get());
        }
        return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfo, externalInfo);
    }

    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByTmdbId(Integer tmdbId, VideoContentFormat format, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        return getVideoContentByTmdbId(tmdbId, videoContentFormatMapper.toVideoContentType(format), hasExternalInfo);
    }

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
            try {
                if (type == MOVIE) {
                    movieDb = tmdbClient.parseMovieById(tmdbId);
                    keywordResults = movieDb.getKeywords().getKeywords();
                    title = movieDb.getTitle();
                } else if (type == TV) {
                    tvSeriesDb = tmdbClient.parseShowById(tmdbId);
                    keywordResults = tvSeriesDb.getKeywords().getResults();
                    title = tvSeriesDb.getName();
                }
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                if(videoContentDb == null){
                    throw new ContentNotFoundException();
                }
                exception = e;
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

            if(exception != null){
                throw new ExternalInfoException(exception.getMessage(), basicInfo);
            }

            externalInfo = type == MOVIE ? externalInfoMapper.fromMovieDb(movieDb) : externalInfoMapper.fromTvSeriesDb(tvSeriesDb);
        } else {
            basicInfo = basicInfoMapper.fromVideoContentDb(optionalVideoContentDb.get());
        }
        return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfo, externalInfo);
    }

    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByImdbId(String imdbId, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        Optional<VideoContent> optionalVideoContent = videoContentRepository.findByImdbId(imdbId);

        if (optionalVideoContent.isPresent()) {
            VideoContent videoContent = optionalVideoContent.get();
            if(hasExternalInfo){
                return videoContent.getCategory() == VideoContentCategory.ANIME ? getVideoContentByAniListId(videoContent.getAniListId(), true) : getVideoContentByTmdbId(videoContent.getTmdbId(), videoContent.getType(), true);
            }else{
                return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfoMapper.fromVideoContentDb(videoContent), null);
            }
        } else {
            throw new ContentNotFoundException();
        }
    }

    public com.writenbite.bisonfun.api.types.videocontent.VideoContent getVideoContentByMalId(Integer malId, boolean hasExternalInfo) throws ContentNotFoundException, ExternalInfoException {
        Optional<VideoContent> optionalVideoContent = videoContentRepository.findByMalId(malId);
        if (optionalVideoContent.isPresent()) {
            VideoContent videoContent = optionalVideoContent.get();
            if(hasExternalInfo) {
                return getVideoContentByAniListId(videoContent.getAniListId(), true);
            }else{
                return new com.writenbite.bisonfun.api.types.videocontent.VideoContent(basicInfoMapper.fromVideoContentDb(videoContent), null);
            }
        } else {
            throw new ContentNotFoundException();
        }
    }

    public VideoContent saveVideoContent(VideoContent videoContent) {
        return videoContentRepository.save(videoContent);
    }

    public Optional<VideoContent> updateContent(VideoContent updatedVideoContent) {
        Optional<VideoContent> existingVideoContent = getVideoContentByVideoContent(updatedVideoContent);
        if (existingVideoContent.isPresent()) {
            VideoContent videoContent = existingVideoContent.get();

            //Update poster if it's not empty String or null
            if (updatedVideoContent.getPoster() != null && !updatedVideoContent.getPoster().trim().isEmpty()) {
                videoContent.setPoster(updatedVideoContent.getPoster());
            }
            //Update category if it's more specified (i.e. Mainstream updates to Anime but Anime not updates to Mainstream)
            if (videoContent.getCategory() == VideoContentCategory.MAINSTREAM) {
                videoContent.setCategory(updatedVideoContent.getCategory());
            }
            //Update type if old type is Unknown
            if (videoContent.getType() == UNKNOWN) {
                videoContent.setType(updatedVideoContent.getType());
            }
            //Update year if old doesn't have one
            if (videoContent.getYear() <= 0) {
                videoContent.setYear(updatedVideoContent.getYear());
            }
            //Update imdb id if old doesn't have one, or it's empty
            if (videoContent.getImdbId() == null || videoContent.getImdbId().trim().isEmpty()) {
                videoContent.setImdbId(updatedVideoContent.getImdbId());
            }
            //Update tmdb id if old doesn't have one
            if (videoContent.getTmdbId() == null || videoContent.getTmdbId() <= 0) {
                videoContent.setTmdbId(updatedVideoContent.getTmdbId());
            }
            //Update mal id if old doesn't have one
            if (videoContent.getMalId() == null || videoContent.getMalId() <= 0) {
                videoContent.setMalId(updatedVideoContent.getMalId());
            }
            //Update anilist id if old doesn't have one
            if (videoContent.getAniListId() == null || videoContent.getAniListId() <= 0) {
                videoContent.setAniListId(updatedVideoContent.getAniListId());
            }

            return Optional.of(videoContentRepository.save(videoContent));
        } else {
            return Optional.empty();
        }
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

    public VideoContent createVideoContent(int aniListId, int tmdbId, VideoContentType type) throws ContentNotFoundException, TooManyAnimeRequestsException, JsonProcessingException {
        if (type == MOVIE) {
            return createMovieVideoContent(aniListId, tmdbId);
        } else if (type == TV) {
            return createTvVideoContent(aniListId, tmdbId);
        } else {
            return videoContentMapper.fromAniListMedia(aniListClient.parseAnimeById(aniListId));
        }
    }

    public VideoContent createMovieVideoContent(int aniListId, int tmdbId) throws ContentNotFoundException, TooManyAnimeRequestsException, JsonProcessingException {
        AniListMedia apiAnime = aniListClient.parseAnimeById(aniListId);
        MovieDb tmdbMovie = tmdbClient.parseMovieById(tmdbId);

        return videoContentMapper.fromModels(apiAnime, tmdbMovie, null);
    }

    public VideoContent createTvVideoContent(int aniListId, int tmdbId) throws ContentNotFoundException, TooManyAnimeRequestsException, JsonProcessingException {
        AniListMedia apiAnime = aniListClient.parseAnimeById(aniListId);
        TvSeriesDb tv = tmdbClient.parseShowById(tmdbId);

        return videoContentMapper.fromModels(apiAnime, null, tv);
    }

    public void saveVideoContentFromUserVideoContentList(List<UserVideoContent> userVideoContentList) {
        for (UserVideoContent userVideoContent : userVideoContentList) {
            VideoContent videoContent = userVideoContent.getVideoContent();
            videoContentRepository.findById(videoContent.getId()).ifPresent(dbContent -> videoContent.setId(dbContent.getId()));
            videoContentRepository.save(videoContent);
        }
    }

    public VideoContent addNewVideoContent(VideoContent videoContent) throws ContentNotFoundException {
        if (videoContent == null) {
            throw new ContentNotFoundException();
        }
        return videoContentRepository.save(videoContent);
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
        } catch (NoAccessException | JsonProcessingException e) {
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
        } catch (NoAccessException | JsonProcessingException e) {
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
