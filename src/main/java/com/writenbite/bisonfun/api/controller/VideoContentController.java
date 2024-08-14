package com.writenbite.bisonfun.api.controller;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.service.ExternalInfoException;
import com.writenbite.bisonfun.api.service.VideoContentService;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import com.writenbite.bisonfun.api.types.videocontent.*;
import com.writenbite.bisonfun.api.types.videocontent.output.TrendVideoContentResponse;
import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class VideoContentController {

    private final VideoContentService videoContentService;

    @Autowired
    public VideoContentController(VideoContentService videoContentService) {
        this.videoContentService = videoContentService;
    }

    @QueryMapping
    public DataFetcherResult<VideoContent> videoContent(@Argument long id, DataFetchingEnvironment environment) throws ContentNotFoundException {
        List<SelectedField> fields = new ArrayList<>(environment.getSelectionSet()
                .getFields());
        boolean hasExternalInfo = fields.stream().anyMatch(field -> field.getQualifiedName().equalsIgnoreCase("ExternalInfo"));

        DataFetcherResult.Builder<VideoContent> builder = DataFetcherResult.newResult();
        try {
            builder.data(videoContentService.getVideoContentById(id, hasExternalInfo));
        } catch (ExternalInfoException e) {
            builder
                    .data(new VideoContent(e.getData(VideoContent.BasicInfo.class).orElse(null), null))
                    .error(
                            GraphQLError.newError()
                                    .errorType(ErrorType.INTERNAL_ERROR)
                                    .message(e.getMessage())
                                    .build()
                    );
        }
        return builder.build();
    }

    @QueryMapping
    public DataFetcherResult<VideoContent> videoContentByAniListId(@Argument Integer aniListId, DataFetchingEnvironment environment) throws ContentNotFoundException {
        List<SelectedField> fields = new ArrayList<>(environment.getSelectionSet()
                .getFields());
        boolean hasExternalInfo = fields.stream().anyMatch(field -> field.getQualifiedName().equalsIgnoreCase("ExternalInfo"));
        DataFetcherResult.Builder<VideoContent> builder = DataFetcherResult.newResult();
        try {
            builder.data(videoContentService.getVideoContentByAniListId(aniListId, hasExternalInfo));
        } catch (ExternalInfoException e) {
            builder.data(new VideoContent(e.getData(VideoContent.BasicInfo.class).orElse(null), null))
                    .error(
                            GraphQLError.newError()
                                    .errorType(ErrorType.INTERNAL_ERROR)
                                    .message(e.getMessage())
                                    .build()
                    );
        }
        return builder.build();
    }

    @QueryMapping
    public DataFetcherResult<VideoContent> videoContentByTmdbId(@Argument Integer tmdbId, @Argument VideoContentFormat format, DataFetchingEnvironment environment) throws ContentNotFoundException {
        List<SelectedField> fields = new ArrayList<>(environment.getSelectionSet()
                .getFields());
        boolean hasExternalInfo = fields.stream().anyMatch(field -> field.getQualifiedName().equalsIgnoreCase("ExternalInfo"));
        DataFetcherResult.Builder<VideoContent> builder = DataFetcherResult.newResult();
        try {
            builder.data(videoContentService.getVideoContentByTmdbId(tmdbId, format, hasExternalInfo));
        } catch (ExternalInfoException e) {
            builder.data(new VideoContent(e.getData(VideoContent.BasicInfo.class).orElse(null), null))
                    .error(
                            GraphQLError.newError()
                                    .errorType(ErrorType.INTERNAL_ERROR)
                                    .message(e.getMessage())
                                    .build()
                    );
        }
        return builder.build();
    }

    @QueryMapping
    public DataFetcherResult<VideoContent> videoContentByMalId(@Argument Integer malId, DataFetchingEnvironment environment) throws ContentNotFoundException {
        List<SelectedField> fields = new ArrayList<>(environment.getSelectionSet()
                .getFields());
        boolean hasExternalInfo = fields.stream().anyMatch(field -> field.getQualifiedName().equalsIgnoreCase("ExternalInfo"));
        DataFetcherResult.Builder<VideoContent> builder = DataFetcherResult.newResult();
        try {
            builder.data(videoContentService.getVideoContentByMalId(malId,hasExternalInfo));
        } catch (ExternalInfoException e) {
            builder.data(new VideoContent(e.getData(VideoContent.BasicInfo.class).orElse(null), null))
                    .error(
                            GraphQLError.newError()
                                    .errorType(ErrorType.INTERNAL_ERROR)
                                    .message(e.getMessage())
                                    .build()
                    );
        }
        return builder.build();
    }

    @QueryMapping
    public DataFetcherResult<VideoContent> videoContentByImdbId(@Argument String imdbId, DataFetchingEnvironment environment) throws ContentNotFoundException {
        List<SelectedField> fields = new ArrayList<>(environment.getSelectionSet()
                .getFields());
        boolean hasExternalInfo = fields.stream().anyMatch(field -> field.getQualifiedName().equalsIgnoreCase("ExternalInfo"));
        DataFetcherResult.Builder<VideoContent> builder = DataFetcherResult.newResult();
        try {
            builder.data(videoContentService.getVideoContentByImdbId(imdbId,hasExternalInfo));
        } catch (ExternalInfoException e) {
            builder.data(new VideoContent(e.getData(VideoContent.BasicInfo.class).orElse(null), null))
                    .error(
                            GraphQLError.newError()
                                    .errorType(ErrorType.INTERNAL_ERROR)
                                    .message(e.getMessage())
                                    .build()
                    );
        }
        return builder.build();
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
    ) throws ContentNotFoundException {
        return DataFetcherResult.<Connection<VideoContent.BasicInfo>>newResult()
                .data(
                        videoContentService.search(query, category, formats, page)
                )
                .build();
    }

    @QueryMapping
    public TrendVideoContentResponse trendVideoContent(){
        return new TrendVideoContentResponse(null, null, null);
    }
    @SchemaMapping(typeName = "TrendVideoContentResponse", field = "animeTrends")
    public List<VideoContent.BasicInfo> animeTrends() throws ContentNotFoundException {
        return videoContentService.getAnimeTrends();
    }
    @SchemaMapping(typeName = "TrendVideoContentResponse", field = "movieTrends")
    public List<VideoContent.BasicInfo> movieTrends() throws ContentNotFoundException {
        return videoContentService.getMovieTrends();
    }
    @SchemaMapping(typeName = "TrendVideoContentResponse", field = "tvTrends")
    public List<VideoContent.BasicInfo> tvTrends() throws ContentNotFoundException {
        return videoContentService.getTvTrends();
    }

}
