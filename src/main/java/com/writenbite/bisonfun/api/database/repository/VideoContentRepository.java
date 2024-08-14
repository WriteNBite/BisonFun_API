package com.writenbite.bisonfun.api.database.repository;

import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VideoContentRepository extends JpaRepository<VideoContent, Long> {

    Optional<VideoContent> findByIdOrTmdbIdOrAniListIdOrImdbIdOrMalId(Long id, Integer tmdbId, Integer aniListId, String imdbId, Integer malId);

    @Override
    @NonNull
    Optional<VideoContent> findById(@NonNull Long aLong);
    Optional<VideoContent> findByTmdbIdAndType(Integer tmdbId, VideoContentType type);
    @Query("SELECT videoContent FROM VideoContent videoContent WHERE videoContent.tmdbId IN :tmdbIds AND videoContent.type = :type")
    List<VideoContent> findListByTmdbIdsAndType(Collection<Integer> tmdbIds, VideoContentType type);
    Optional<VideoContent> findByAniListId(Integer aniListId);
    @Query("SELECT videoContent FROM VideoContent videoContent WHERE videoContent.aniListId IN :aniListIds")
    List<VideoContent> findListByAniListIds(Collection<Integer> aniListIds);
    Optional<VideoContent> findByImdbId(String imdbId);
    Optional<VideoContent> findByMalId(Integer malId);

    boolean existsByAniListId(Integer aniListId);

    boolean existsByTmdbIdAndType(Integer tmdbId, VideoContentType type);
}