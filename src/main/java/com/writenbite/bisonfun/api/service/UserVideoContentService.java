package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.*;
import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaStatus;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.database.entity.*;
import com.writenbite.bisonfun.api.database.mapper.VideoContentCategoryMapper;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import com.writenbite.bisonfun.api.database.repository.UserVideoContentPageableRepository;
import com.writenbite.bisonfun.api.database.repository.UserVideoContentRepository;
import com.writenbite.bisonfun.api.service.external.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.types.*;
import com.writenbite.bisonfun.api.types.mapper.UserVideoContentListElementMapper;
import com.writenbite.bisonfun.api.types.mapper.UserVideoContentListStatusMapper;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UpdateUserVideoContentListElementInput;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UpdateUserVideoContentListElementPayload;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UserVideoContentListInput;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static com.writenbite.bisonfun.api.database.entity.VideoContentCategory.ANIME;
import static com.writenbite.bisonfun.api.database.entity.VideoContentCategory.MAINSTREAM;

@Slf4j
@Service
@AllArgsConstructor
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
    private final VideoContentMapper videoContentDbMapper;
    private final VideoContentService videoContentService;

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
                        yield Optional.of(tmdbClient.parseShowById(videoContent.getTmdbId()));
                    } catch (ContentNotFoundException e) {
                        yield Optional.empty();
                    }
                }
                case MOVIE -> {
                    try {
                        yield Optional.of(tmdbClient.parseMovieById(videoContent.getTmdbId()));
                    } catch (ContentNotFoundException e) {
                        yield Optional.empty();
                    }
                }
                case UNKNOWN -> Optional.empty();
            };
        }
        return Optional.empty();
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

    public boolean deleteVideoContentFromUserList(int userId, VideoContentIdInput input){
        Optional<UserVideoContent> optionalUserVideoContent = getUserVideoContentByIdInput(userId, input);
        if(optionalUserVideoContent.isEmpty()){
            throw new IllegalArgumentException("User " + userId + " have no video content by " + input + " input");
        }
        userVideoContentRepository.delete(optionalUserVideoContent.get());
        return getUserVideoContentByIdInput(userId, input).isEmpty();
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
        PageInfo pageInfo = new PageInfo.PageInfoBuilder().increaseTotal((int) result.getTotalElements()).setPerPage(result.getNumberOfElements()).setCurrentPageIfLess(result.getNumber() + 1).setLastPageIfGreater(result.getTotalPages()).setHasNextPage(result.hasNext()).createPageInfo();
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
        return videoContentDbMapper.toBasicInfo(result.getContent().getFirst().getVideoContent());
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

    public UpdateUserVideoContentListElementPayload updateUserVideoContentListElement(User user, UpdateUserVideoContentListElementInput input) throws TooManyAnimeRequestsException, ContentNotFoundException {
        UserVideoContent userVideoContent = getOrCreateUserVideoContent(user, input);

        updateScore(userVideoContent, input.score());
        updateEpisodesAndStatus(userVideoContent, input);

        userVideoContent = saveUserVideoContent(userVideoContent);

        return new UpdateUserVideoContentListElementPayload(userVideoContentListElementMapper.fromEntity(userVideoContent));
    }

    private UserVideoContent getOrCreateUserVideoContent(User user, UpdateUserVideoContentListElementInput input) throws ContentNotFoundException, TooManyAnimeRequestsException {
        Optional<UserVideoContent> optionalUserVideoContent = getUserVideoContentByIdInput(user.getId(), input.videoContentIdInput());
        if (optionalUserVideoContent.isPresent()) {
            return optionalUserVideoContent.get();
        } else {
            VideoContent videoContent = videoContentService.getOrCreateVideoContent(input.videoContentIdInput());
            UserVideoContentId userVideoContentId = new UserVideoContentId(user.getId(), videoContent.getId());
            return new UserVideoContent(userVideoContentId, videoContent, 0, (input.score() != null ? input.score() : 0), UserVideoContentStatus.PLANNED, 1, user);
        }
    }

    private void updateScore(UserVideoContent userVideoContent, Integer score) {
        if (scoreCheck(score) != null) {
            userVideoContent.setScore(score);
        }
    }

    private void updateEpisodesAndStatus(UserVideoContent userVideoContent, UpdateUserVideoContentListElementInput input)
            throws TooManyAnimeRequestsException {

        boolean isEpisodeInputExist = input.episodes() != null;
        boolean isStatusInputExist = input.status() != null;

        if (isEpisodeInputExist || isStatusInputExist) {
            Integer watchedEpisodes = isEpisodeInputExist ? input.episodes() : userVideoContent.getEpisodes();
            UserVideoContentStatus status = isStatusInputExist ? userVideoContentListStatusMapper.toUserVideoContentStatus(input.status()) : userVideoContent.getStatus();

            Optional<VideoContentModel> optionalVideoContentModel = getVideoContentModel(userVideoContent.getVideoContent());
            if (optionalVideoContentModel.isPresent()) {
                PairRelationHandler<Integer, UserVideoContentStatus> episodeStatusRelationHandler = new EpisodeStatusRelationHandler(optionalVideoContentModel.get());
                Pair<Integer, UserVideoContentStatus> pair = episodeStatusRelationHandler.handleRelation(watchedEpisodes, status, (isEpisodeInputExist ? PairRelationHandler.Priority.LEFT : PairRelationHandler.Priority.RIGHT));
                watchedEpisodes = pair.getLeft();
                status = pair.getRight();
            }

            userVideoContent.setEpisodes(watchedEpisodes);
            userVideoContent.setStatus(status);
        }
    }

    public Optional<UserVideoContent> getUserVideoContentByIdInput(int userId, VideoContentIdInput input) {
        Optional<UserVideoContent> optionalUserVideoContent = Optional.empty();
        if (input.videoContentId() != null) {
            optionalUserVideoContent = getUserVideoContentById(userId, input.videoContentId());
        }
        if (input.aniListId() != null) {
            optionalUserVideoContent = optionalUserVideoContent.isEmpty() ? userVideoContentRepository.findById_UserIdAndVideoContent_AniListId(userId, input.aniListId()) : optionalUserVideoContent;
        }
        if (input.tmdbVideoContentIdInput() != null) {
            optionalUserVideoContent = optionalUserVideoContent.isEmpty() ?
                    userVideoContentRepository.findById_UserIdAndVideoContent_TmdbIdAndVideoContent_Type(
                            userId,
                            input.tmdbVideoContentIdInput().tmdbId(),
                            formatMapper.toVideoContentType(input.tmdbVideoContentIdInput().format())
                    )
                    : optionalUserVideoContent;
        }
        return optionalUserVideoContent;
    }
}
