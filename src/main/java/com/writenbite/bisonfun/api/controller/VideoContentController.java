package com.writenbite.bisonfun.api.controller;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.types.media.AniListMedia;
import com.writenbite.bisonfun.api.client.tmdb.types.TmdbVideoContent;
import com.writenbite.bisonfun.api.service.*;
import com.writenbite.bisonfun.api.service.external.AnimeService;
import com.writenbite.bisonfun.api.service.external.MainstreamService;
import com.writenbite.bisonfun.api.service.external.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.service.search.VideoContentSearchQuery;
import com.writenbite.bisonfun.api.service.search.VideoContentSearchService;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.*;
import com.writenbite.bisonfun.api.types.videocontent.output.TrendVideoContentResponse;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class VideoContentController {

    private final VideoContentService videoContentService;
    private final VideoContentSearchService videoContentSearchService;
    private final AnimeService<AniListMedia, com.writenbite.bisonfun.api.database.entity.VideoContent> animeService;
    private final MainstreamService<TmdbVideoContent, com.writenbite.bisonfun.api.database.entity.VideoContent, AniListMedia> mainstreamService;

    @Autowired
    public VideoContentController(VideoContentService videoContentService, VideoContentSearchService videoContentSearchService, AnimeService<AniListMedia, com.writenbite.bisonfun.api.database.entity.VideoContent> animeService, MainstreamService<TmdbVideoContent, com.writenbite.bisonfun.api.database.entity.VideoContent, AniListMedia> mainstreamService) {
        this.videoContentService = videoContentService;
        this.videoContentSearchService = videoContentSearchService;
        this.animeService = animeService;
        this.mainstreamService = mainstreamService;
    }

    @QueryMapping
    public DataFetcherResult<VideoContent> videoContentByIdInput(@Argument VideoContentIdInput input, DataFetchingEnvironment environment) throws ContentNotFoundException, TooManyAnimeRequestsException, ExternalInfoException {
        List<SelectedField> fields = new ArrayList<>(environment.getSelectionSet()
                .getFields());
        boolean hasExternalInfo = fields.stream().anyMatch(field -> field.getQualifiedName().equalsIgnoreCase("ExternalInfo"));
        DataFetcherResult.Builder<VideoContent> builder = DataFetcherResult.newResult();
        builder.data(videoContentService.getVideoContentByIdInput(input, hasExternalInfo));
        return builder.build();
    }

    @QueryMapping
    public DataFetcherResult<Connection<VideoContent.BasicInfo>> search(
            @Argument String query,
            @Argument VideoContentCategory category,
            @Argument List<VideoContentFormat> formats,
            @Argument Integer page
    ) throws ContentNotFoundException, TooManyAnimeRequestsException {
        return DataFetcherResult.<Connection<VideoContent.BasicInfo>>newResult()
                .data(
                        videoContentSearchService.search(new VideoContentSearchQuery(query, category, formats, page))
                )
                .build();
    }

    @QueryMapping
    public TrendVideoContentResponse trendVideoContent(){
        return new TrendVideoContentResponse(null, null, null);
    }
    @SchemaMapping(typeName = "TrendVideoContentResponse", field = "animeTrends")
    public List<VideoContent.BasicInfo> animeTrends() throws ContentNotFoundException, TooManyAnimeRequestsException {
        return animeService.getAnimeTrends();
    }
    @SchemaMapping(typeName = "TrendVideoContentResponse", field = "movieTrends")
    public List<VideoContent.BasicInfo> movieTrends() throws ContentNotFoundException {
        return mainstreamService.getTrends(VideoContentFormat.MOVIE);
    }
    @SchemaMapping(typeName = "TrendVideoContentResponse", field = "tvTrends")
    public List<VideoContent.BasicInfo> tvTrends() throws ContentNotFoundException {
        return mainstreamService.getTrends(VideoContentFormat.TV);
    }

}
