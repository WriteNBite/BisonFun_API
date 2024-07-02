package com.writenbite.bisonfun.api.database.repository;

import com.writenbite.bisonfun.api.database.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface UserVideoContentPageableRepository extends PagingAndSortingRepository<UserVideoContent, UserVideoContentId> {
    @Query(
            "SELECT userVideoContent FROM UserVideoContent userVideoContent " +
                    "WHERE userVideoContent.user.id = :userId " +
                    "AND (:episodes IS NULL OR userVideoContent.episodes = :episodes) " +
                    "AND (:score IS NULL OR userVideoContent.score = :score) " +
                    "AND (:statuses IS NULL OR userVideoContent.status IN :statuses) " +
                    "AND (:categories IS NULL OR userVideoContent.videoContent.category IN :categories) " +
                    "AND (:types IS NULL OR userVideoContent.videoContent.type IN :types) " +
                    "AND (:yearFrom IS NULL OR userVideoContent.videoContent.year >= :yearFrom) " +
                    "AND (:yearTo IS NULL OR userVideoContent.videoContent.year <= :yearTo)"
    )
    Page<UserVideoContent> findUserVideoContent(
            @Param("userId") int userId,
            @Param("episodes") Integer episode,
            @Param("score") Integer score,
            @Param("statuses") Collection<UserVideoContentStatus> statuses,
            @Param("categories") Collection<VideoContentCategory> categories,
            @Param("types") Collection<VideoContentType> types,
            @Param("yearFrom") Integer yearFrom,
            @Param("yearTo") Integer yearTo,
            Pageable pageable
    );
    @Query(
            "SELECT userVideoContent FROM UserVideoContent userVideoContent " +
                    "WHERE userVideoContent.user.id = :userId " +
                    "AND (:episodes IS NULL OR userVideoContent.episodes = :episodes) " +
                    "AND (:score IS NULL OR userVideoContent.score = :score) " +
                    "AND (:statuses IS NULL OR userVideoContent.status IN :statuses) " +
                    "AND (:categories IS NULL OR userVideoContent.videoContent.category IN :categories) " +
                    "AND (:types IS NULL OR userVideoContent.videoContent.type IN :types) " +
                    "AND (:yearFrom IS NULL OR userVideoContent.videoContent.year >= :yearFrom) " +
                    "AND (:yearTo IS NULL OR userVideoContent.videoContent.year <= :yearTo) "
    )
    Slice<UserVideoContent> findUserVideoContentSlice(
            @Param("userId") int userId,
            @Param("episodes") Integer episode,
            @Param("score") Integer score,
            @Param("statuses") Collection<UserVideoContentStatus> statuses,
            @Param("categories") Collection<VideoContentCategory> categories,
            @Param("types") Collection<VideoContentType> types,
            @Param("yearFrom") Integer yearFrom,
            @Param("yearTo") Integer yearTo,
            Pageable pageable
    );

    @Query(
            "SELECT COUNT(userVideoContent) FROM UserVideoContent userVideoContent " +
                    "WHERE userVideoContent.user.id = :userId " +
                    "AND (:episodes IS NULL OR userVideoContent.episodes = :episodes) " +
                    "AND (:score IS NULL OR userVideoContent.score = :score) " +
                    "AND (:statuses IS NULL OR userVideoContent.status IN :statuses) " +
                    "AND (:categories IS NULL OR userVideoContent.videoContent.category IN :categories) " +
                    "AND (:types IS NULL OR userVideoContent.videoContent.type IN :types) " +
                    "AND (:yearFrom IS NULL OR userVideoContent.videoContent.year >= :yearFrom) " +
                    "AND (:yearTo IS NULL OR userVideoContent.videoContent.year <= :yearTo) "
    )
    int countUserVideoContent(
            @Param("userId") int userId,
            @Param("episodes") Integer episode,
            @Param("score") Integer score,
            @Param("statuses") Collection<UserVideoContentStatus> statuses,
            @Param("categories") Collection<VideoContentCategory> categories,
            @Param("types") Collection<VideoContentType> types,
            @Param("yearFrom") Integer yearFrom,
            @Param("yearTo") Integer yearTo
    );

}
