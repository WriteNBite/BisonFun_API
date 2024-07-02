package com.writenbite.bisonfun.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.writenbite.bisonfun.api.client.*;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaStatus;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.database.entity.*;
import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.database.mapper.VideoContentCategoryMapper;
import com.writenbite.bisonfun.api.database.repository.UserVideoContentPageableRepository;
import com.writenbite.bisonfun.api.database.repository.UserVideoContentRepository;
import com.writenbite.bisonfun.api.types.*;
import com.writenbite.bisonfun.api.types.mapper.UserVideoContentListElementMapper;
import com.writenbite.bisonfun.api.types.mapper.UserVideoContentListStatusMapper;
import com.writenbite.bisonfun.api.types.mapper.VideoContentBasicInfoMapper;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UserVideoContentListInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static com.writenbite.bisonfun.api.database.entity.VideoContentCategory.ANIME;
import static com.writenbite.bisonfun.api.database.entity.VideoContentCategory.MAINSTREAM;

@Slf4j
@Service
public class UserVideoContentService {
    private final UserVideoContentRepository userVideoContentRepository;
    private final UserVideoContentPageableRepository userVideoContentPageableRepository;
    private final TmdbClient tmdbClient;
    private final AniListClient aniListClient;
    private final Converter<AniListMediaStatus, VideoContentModel.Status> statusConverter;
    private final UserVideoContentListStatusMapper userVideoContentListStatusMapper;
    private final VideoContentCategoryMapper categoryMapper;
    private final VideoContentFormatMapper formatMapper;
    private final UserVideoContentListElementMapper userVideoContentListElementMapper;
    private final VideoContentBasicInfoMapper videoContentBasicInfoMapper;

    @Autowired
    public UserVideoContentService(UserVideoContentRepository userVideoContentRepository, UserVideoContentPageableRepository userVideoContentPageableRepository, TmdbClient tmdbClient, AniListClient aniListClient, Converter<AniListMediaStatus, VideoContentModel.Status> statusConverter, UserVideoContentListStatusMapper userVideoContentListStatusMapper, VideoContentCategoryMapper categoryMapper, VideoContentFormatMapper formatMapper, UserVideoContentListElementMapper userVideoContentListElementMapper, VideoContentBasicInfoMapper videoContentBasicInfoMapper) {
        this.userVideoContentRepository = userVideoContentRepository;
        this.userVideoContentPageableRepository = userVideoContentPageableRepository;
        this.tmdbClient = tmdbClient;
        this.aniListClient = aniListClient;
        this.statusConverter = statusConverter;
        this.userVideoContentListStatusMapper = userVideoContentListStatusMapper;
        this.categoryMapper = categoryMapper;
        this.formatMapper = formatMapper;
        this.userVideoContentListElementMapper = userVideoContentListElementMapper;
        this.videoContentBasicInfoMapper = videoContentBasicInfoMapper;
    }

    public void updateUserVideoContent(UserVideoContent userVideoContent, User user, VideoContent videoContent) throws TooManyAnimeRequestsException {
        UserVideoContentId userVideoContentId = new UserVideoContentId(user.getId(), videoContent.getId());
        userVideoContent.setId(userVideoContentId);
        userVideoContent.setUser(user);
        userVideoContent.setVideoContent(videoContent);

        Optional<UserVideoContent> dbUserVideoContent = getUserVideoContentById(userVideoContentId);
        if (dbUserVideoContent.isPresent()) {
            if (!Objects.equals(userVideoContent.getEpisodes(), dbUserVideoContent.get().getEpisodes())) {//if episode number changed
                userVideoContent.setStatus(updateStatus(userVideoContent));
            } else if (userVideoContent.getStatus() != dbUserVideoContent.get().getStatus()) {//if status changed
                userVideoContent.setEpisodes(updateEpisodes(userVideoContent));
            }
        }
        saveUserVideoContent(userVideoContent);
    }

    private Optional<VideoContentModel> getVideoContentModel(VideoContent videoContent) throws TooManyAnimeRequestsException {
        if (videoContent.getCategory() == ANIME) {
            try {
                return Optional.of(new AniListVideoContentModel(aniListClient.parseAnimeById(videoContent.getAniListId()), statusConverter));
            } catch (ContentNotFoundException e) {
                return Optional.empty();
            }
        } else if (videoContent.getCategory() == MAINSTREAM) {
            return switch (videoContent.getType()) {
                case TV, MUSIC, SPECIAL -> {
                    try {
                        yield Optional.of(new TmdbTvSeriesVideoContentModel(tmdbClient.parseShowById(videoContent.getTmdbId())));
                    } catch (JsonProcessingException e) {
                        yield Optional.empty();
                    }
                }
                case MOVIE -> {
                    try {
                        yield Optional.of(new TmdbMovieVideoContentModel(tmdbClient.parseMovieById(videoContent.getTmdbId())));
                    } catch (JsonProcessingException e) {
                        yield Optional.empty();
                    }
                }
                case UNKNOWN -> Optional.empty();
            };
        }
        return Optional.empty();
    }

    private UserVideoContentStatus updateStatus(UserVideoContent userVideoContent) throws TooManyAnimeRequestsException {
        Optional<VideoContentModel> optionalVideoContentModel = getVideoContentModel(userVideoContent.getVideoContent());
        if (optionalVideoContentModel.isPresent()) {
            VideoContentModel videoContentModel = optionalVideoContentModel.get();
            if ((userVideoContent.getEpisodes() > 0 || userVideoContent.getStatus() == UserVideoContentStatus.COMPLETE) && userVideoContent.getEpisodes() < videoContentModel.getEpisodes()) {//if less watched episodes as it is make it watching
                return UserVideoContentStatus.WATCHING;
            } else if (videoContentModel.getStatus() == VideoContentModel.Status.RELEASED && userVideoContent.getEpisodes() > 0 && userVideoContent.getEpisodes() == videoContentModel.getEpisodes()) {// if it released and all episodes watched then it completed
                return UserVideoContentStatus.COMPLETE;
            }
            return userVideoContent.getStatus();
        }
        return userVideoContent.getStatus();
    }

    private int updateEpisodes(UserVideoContent userVideoContent) throws TooManyAnimeRequestsException {
        Optional<VideoContentModel> optionalVideoContentModel = getVideoContentModel(userVideoContent.getVideoContent());
        if (optionalVideoContentModel.isPresent()) {
            VideoContentModel videoContentModel = optionalVideoContentModel.get();
            if (userVideoContent.getStatus() == UserVideoContentStatus.COMPLETE) {//if complete then make all episodes watched
                return videoContentModel.getEpisodes();
            } else if (userVideoContent.getStatus() == UserVideoContentStatus.PLANNED) {// if planned then 0 episodes watched
                return 0;
            }
            return userVideoContent.getEpisodes();
        }
        return userVideoContent.getEpisodes();
    }

    private Optional<UserVideoContent> getUserVideoContentById(UserVideoContentId userVideoContentKey) {
        log.info("Get User Video Content by User {} and VideoContent {} by UserVideoContentKey", userVideoContentKey.getUserId(), userVideoContentKey.getVideoContentId());
        Optional<UserVideoContent> userVideoContent = userVideoContentRepository.findById(userVideoContentKey);
        log.debug("UserVideoContent: " + userVideoContent);
        return userVideoContent;
    }

    public Optional<UserVideoContent> getUserVideoContentById(int userId, long videoContentId) {
        log.info("Get User Video Content by User {} and Video Content {}", userId, videoContentId);
        return getUserVideoContentById(new UserVideoContentId(userId, videoContentId));
    }

    private UserVideoContent saveUserVideoContent(UserVideoContent userVideoContent) {
        log.info("Save Video Content {} in User {} list", userVideoContent.getVideoContent().getId(), userVideoContent.getUser().getId());
        return userVideoContentRepository.save(userVideoContent);
    }

    public void deleteVideoContentFromUserList(UserVideoContentId userVideoContentKey) {
        log.info("Delete Video Content {} from User {} list", userVideoContentKey.getVideoContentId(), userVideoContentKey.getUserId());
        userVideoContentRepository.findById(userVideoContentKey).ifPresent(userVideoContentRepository::delete);
    }

    public void saveUserList(User user, List<UserVideoContent> videoContentList) {
        for (UserVideoContent userVideoContent : videoContentList) {
            UserVideoContent dbUserVideoContent = userVideoContentRepository.findById(new UserVideoContentId(user.getId(), userVideoContent.getVideoContent().getId())).orElse(null);
            if (dbUserVideoContent != null) {
                //set pk
                userVideoContent.setId(dbUserVideoContent.getId());
                //set the biggest score
                userVideoContent.setScore(Math.max(dbUserVideoContent.getScore(), userVideoContent.getScore()));

                // set db progress and status if it has bigger progress
                if (dbUserVideoContent.getEpisodes() > userVideoContent.getEpisodes()) {
                    userVideoContent.setEpisodes(dbUserVideoContent.getEpisodes());
                    userVideoContent.setScore(dbUserVideoContent.getScore());
                } else if (dbUserVideoContent.getEpisodes().equals(userVideoContent.getEpisodes())) {
                    userVideoContent.setStatus(userVideoContent.getStatus().getStage() > dbUserVideoContent.getStatus().getStage() ? userVideoContent.getStatus() : dbUserVideoContent.getStatus());
                }
            } else {
                //set pk
                userVideoContent.setId(new UserVideoContentId(user.getId(), userVideoContent.getVideoContent().getId()));
            }
            //set user
            userVideoContent.setUser(user);
            userVideoContentRepository.save(userVideoContent);
        }
    }

    public Connection<UserVideoContentListElement> userVideoContentList(int userId, UserVideoContentListInput input, Integer page) {
        input = input == null ? new UserVideoContentListInput(null, null, null, null, null, null, null) : input;
        Integer queryScore = scoreCheck(input.score());
        List<UserVideoContentStatus> statuses = listMappers(input.statuses(), userVideoContentListStatusMapper::toUserVideoContentStatus);
        List<VideoContentCategory> categories = listMappers(input.categories(), categoryMapper::fromApi);
        List<VideoContentType> types = listMappers(input.formats(), formatMapper::toVideoContentType);

        Pageable pageable = PageRequest.of(page - 1, 20, Sort.by("statusStage"));
        Page<UserVideoContent> result = userVideoContentPageableRepository.findUserVideoContent(userId, input.episode(), queryScore, statuses.isEmpty() ? null : statuses, categories.isEmpty() ? null : categories, types.isEmpty() ? null : types, input.yearFrom(), input.yearTo(), pageable);
        PageInfo pageInfo = new PageInfo((int) result.getTotalElements(), result.getNumberOfElements(), result.getNumber() + 1, result.getTotalPages(), result.hasNext());
        List<UserVideoContentListElement> userVideoContentListElements = userVideoContentListElementMapper.fromEntities(result.getContent());
        return new UserVideoContentListConnection(userVideoContentListElements, pageInfo);
    }

    public com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo whatToWatch(int userId, UserVideoContentListInput input) {
        input = input == null ? new UserVideoContentListInput(null, null, null, null, null, null, null) : input;
        Integer queryScore = scoreCheck(input.score());
        List<UserVideoContentStatus> statuses = listMappers(input.statuses(), userVideoContentListStatusMapper::toUserVideoContentStatus);
        List<VideoContentCategory> categories = listMappers(input.categories(), categoryMapper::fromApi);
        List<VideoContentType> types = listMappers(input.formats(), formatMapper::toVideoContentType);

        int size = userVideoContentPageableRepository.countUserVideoContent(userId, input.episode(), queryScore, statuses.isEmpty() ? null : statuses, categories.isEmpty() ? null : categories, types.isEmpty() ? null : types, input.yearFrom(), input.yearTo());
        Pageable pageable = PageRequest.of(new Random().nextInt(size), 1);
        Slice<UserVideoContent> result = userVideoContentPageableRepository.findUserVideoContentSlice(userId, input.episode(), queryScore, statuses.isEmpty() ? null : statuses, categories.isEmpty() ? null : categories, types.isEmpty() ? null : types, input.yearFrom(), input.yearTo(), pageable);
        return videoContentBasicInfoMapper.fromVideoContentDb(result.getContent().getFirst().getVideoContent());
    }

    public UserVideoContentListElement userVideoContentListElement(int userId, long videoContentId) throws ContentNotFoundException {
        Optional<UserVideoContent> optionalUserVideoContent = getUserVideoContentById(userId, videoContentId);
        UserVideoContent userVideoContent;
        if (optionalUserVideoContent.isPresent()) {
            userVideoContent = optionalUserVideoContent.get();
        } else {
            throw new ContentNotFoundException();
        }
        return userVideoContentListElementMapper.fromEntity(userVideoContent);
    }

    private Integer scoreCheck(Integer score) {
        Integer queryScore = null;
        if (score != null && score >= 0 && score <= 10) {
            queryScore = score;
        }
        return queryScore;
    }

    private <T, B> List<B> listMappers(List<T> list, Function<T, B> converter) {
        List<B> bList = new ArrayList<>();
        if (list != null) {
            for (T t : list) {
                bList.add(converter.apply(t));
            }
        }
        return bList;
    }
}
