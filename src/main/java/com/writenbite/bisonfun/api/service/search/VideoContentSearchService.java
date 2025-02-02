package com.writenbite.bisonfun.api.service.search;

import com.writenbite.bisonfun.api.client.anilist.AniListClient;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbSimpleVideoContent;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContentResultsPage;
import com.writenbite.bisonfun.api.service.external.DatabaseService;
import com.writenbite.bisonfun.api.service.external.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.client.anilist.types.AniListPage;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMediaFormat;
import com.writenbite.bisonfun.api.client.tmdb.TmdbClient;
import com.writenbite.bisonfun.api.service.RawVideoContentFactory;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.mapper.VideoContentFormatMapper;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;
import com.writenbite.bisonfun.api.types.videocontent.output.BasicInfoConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class VideoContentSearchService {
    private final TmdbClient tmdbClient;
    private final RawVideoContentFactory rawVideoContentFactory;
    private final VideoContentFormatMapper videoContentFormatMapper;
    private final AniListClient aniListClient;
    private final DatabaseService<com.writenbite.bisonfun.api.database.entity.VideoContent, TmdbSimpleVideoContent, AniListMedia> databaseService;

    public VideoContentSearchService(TmdbClient tmdbClient, RawVideoContentFactory rawVideoContentFactory, VideoContentFormatMapper videoContentFormatMapper, AniListClient aniListClient, DatabaseService<com.writenbite.bisonfun.api.database.entity.VideoContent, TmdbSimpleVideoContent, AniListMedia> databaseService) {
        this.tmdbClient = tmdbClient;
        this.rawVideoContentFactory = rawVideoContentFactory;
        this.videoContentFormatMapper = videoContentFormatMapper;
        this.aniListClient = aniListClient;
        this.databaseService = databaseService;
    }

    public Connection<VideoContent.BasicInfo> search(VideoContentSearchQuery searchQuery) throws TooManyAnimeRequestsException {
        return switch (searchQuery.category()) {
            case MAINSTREAM -> searchMainstream(searchQuery);
            case ANIME -> searchAnime(searchQuery);
        };
    }

    // Mainstream
    private Connection<VideoContent.BasicInfo> searchMainstream(VideoContentSearchQuery searchQuery){
        PageInfo.PageInfoBuilder pageInfoBuilder = new PageInfo.PageInfoBuilder()
                .setCurrentPageIfLess(searchQuery.page());

        List<TmdbVideoContentResultsPage> searchResults = new ArrayList<>();
        if(searchQuery.formats().contains(VideoContentFormat.MOVIE)){
            searchResults.add(tmdbClient.parseMovieList(searchQuery.query(), searchQuery.page()));
        }
        if(searchQuery.formats().contains(VideoContentFormat.TV)){
            searchResults.add(tmdbClient.parseTVList(searchQuery.query(), searchQuery.page()));
        }
        for (TmdbVideoContentResultsPage resultsPage : searchResults) {
            pageInfoBuilder.setLastPageIfGreater(resultsPage.totalPages())
                    .increaseTotal(resultsPage.totalResults())
                    .setCurrentPageIfLess(resultsPage.page());
        }

        Map<TmdbSimpleVideoContent, Optional<com.writenbite.bisonfun.api.database.entity.VideoContent>> videoContentMap = databaseService.getMainstreamContentMap(searchResults.stream().flatMap(result -> result.results().stream()).toList());
        List<VideoContent.BasicInfo> basicInfoList = videoContentMap.keySet()
                .stream()
                .map(mainstreamVideoContent -> rawVideoContentFactory.toBasicInfo(mainstreamVideoContent, videoContentMap.getOrDefault(mainstreamVideoContent, null).orElse(null)))
                .toList();
        return new BasicInfoConnection(
                basicInfoList,
                pageInfoBuilder.setPerPage(basicInfoList.size())
                        .createPageInfo()
        );
    }

    //Anime
    private Connection<VideoContent.BasicInfo> searchAnime(VideoContentSearchQuery searchQuery) throws TooManyAnimeRequestsException {
        Collection<AniListMediaFormat> mediaFormats = videoContentFormatMapper.toAniListMediaFormat(searchQuery.formats());

        AniListPage<AniListMedia> aniListResults = aniListClient.parse(searchQuery.query(), searchQuery.page(), mediaFormats);

        PageInfo.PageInfoBuilder pageInfoBuilder = new PageInfo.PageInfoBuilder()
                .setLastPageIfGreater(aniListResults.getPageInfo().lastPage())
                .setPerPage(aniListResults.getPageInfo().perPage())
                .increaseTotal(aniListResults.getPageInfo().lastPage() * aniListResults.getPageInfo().perPage())
                .setCurrentPageIfLess(Math.min(searchQuery.page(), aniListResults.getPageInfo().currentPage()));

        Map<AniListMedia, Optional<com.writenbite.bisonfun.api.database.entity.VideoContent>> aniListMediaVideoContentMap = databaseService.getAnimeContentMap(aniListResults.getList());
        return new BasicInfoConnection(
                aniListMediaVideoContentMap.keySet()
                        .stream()
                        .map(media -> rawVideoContentFactory.toBasicInfo(aniListMediaVideoContentMap.getOrDefault(media, null).orElse(null), media))
                        .toList(),
                pageInfoBuilder.createPageInfo()
        );
    }
}
