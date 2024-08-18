package com.writenbite.bisonfun.api.database.repository;

import com.writenbite.bisonfun.api.database.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVideoContentRepository extends JpaRepository<UserVideoContent, UserVideoContentId> {
    @Query("SELECT userVideoContent FROM UserVideoContent userVideoContent WHERE userVideoContent.id.userId = ?1 AND userVideoContent.videoContent.category IN ?2 AND userVideoContent.status IN ?3 AND userVideoContent.videoContent.type IN ?4")
    List<UserVideoContent> findUserVideoContentByUserIdAndCategoriesAndStatusesAndTypes(int userId, List<VideoContentCategory> categories, List<UserVideoContentStatus> statuses, List<VideoContentType> types);

    @Query("SELECT COUNT(userVideoContent) FROM UserVideoContent userVideoContent WHERE userVideoContent.id.userId = ?1 AND userVideoContent.videoContent.category IN ?2 AND userVideoContent.status IN ?3 AND userVideoContent.videoContent.type IN ?4")
    Long countUserVideoContentByUserIdAndCategoriesAndStatusesAndTypes(int userId, List<VideoContentCategory> categories, List<UserVideoContentStatus> statuses, List<VideoContentType> types);

    @Query("SELECT SUM(userVideoContent.episodes) FROM UserVideoContent userVideoContent WHERE userVideoContent.id.userId = ?1 AND userVideoContent.videoContent.category IN ?2 AND userVideoContent.videoContent.type IN ?3")
    Integer countWatchedEpisodesByUserAndCategoriesAndTypes(int userId, List<VideoContentCategory> categories, List<VideoContentType> types);

    @Query("SELECT AVG(userVideoContent.score) FROM UserVideoContent userVideoContent WHERE userVideoContent.id.userId = ?1 AND userVideoContent.score > 0 AND userVideoContent.videoContent.category IN ?2 AND userVideoContent.videoContent.type IN ?3")
    Float getMeanScoreByUserAndCategoriesAndTypes(int userId, List<VideoContentCategory> categories, List<VideoContentType> types);

    long countDistinctVideoContentById_UserId(Integer userId);

    Optional<UserVideoContent> findById_UserIdAndVideoContent_AniListId(Integer userId, Integer aniListId);

    Optional<UserVideoContent> findById_UserIdAndVideoContent_TmdbIdAndVideoContent_Type(Integer userId, Integer tmdbId, VideoContentType type);

}