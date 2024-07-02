package com.writenbite.bisonfun.api.query;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.config.GraphQlConfig;
import com.writenbite.bisonfun.api.config.MapperConfig;
import com.writenbite.bisonfun.api.controller.VideoContentController;
import com.writenbite.bisonfun.api.service.ExternalInfoException;
import com.writenbite.bisonfun.api.service.VideoContentService;
import com.writenbite.bisonfun.api.types.videocontent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@GraphQlTest(VideoContentController.class)
@Import({MapperConfig.class, GraphQlConfig.class})
public class VideoContentControllerTest {

    @Autowired
    private GraphQlTester tester;

    @MockBean
    private VideoContentService videoContentService;

    private String videoContentQuery;
    private String videoContentByAniListIdQuery;
    private String videoContentByTmdbIdQuery;
    private String videoContentByMalIdQuery;
    private String videoContentByImdbIdQuery;
    private VideoContent videoContent;

    @BeforeEach
    public void setUpVideoContent() throws IOException {
        //Get videoContent query
        Resource videoContentQueryResource = new ClassPathResource("graphql-test/api/videoContentTest.graphql");
        videoContentQuery = new String(Files.readAllBytes(Paths.get(videoContentQueryResource.getURI())));
        //Get videoContentByAniListId query
        Resource videoContentByAniListQueryResource = new ClassPathResource("graphql-test/api/videoContentByAniListIdTest.graphql");
        videoContentByAniListIdQuery = new String(Files.readAllBytes(Paths.get(videoContentByAniListQueryResource.getURI())));
        //Get videoContentByTmdbId query
        Resource videoContentByTmdbQueryResource = new ClassPathResource("graphql-test/api/videoContentByTmdbIdTest.graphql");
        videoContentByTmdbIdQuery = new String(Files.readAllBytes(Paths.get(videoContentByTmdbQueryResource.getURI())));
        //Get videoContentByMalId query
        Resource videoContentByMalQueryResource = new ClassPathResource("graphql-test/api/videoContentByMalIdTest.graphql");
        videoContentByMalIdQuery = new String(Files.readAllBytes(Paths.get(videoContentByMalQueryResource.getURI())));
        //Get videoContentByImdbId query
        Resource videoContentByImdbQueryResource = new ClassPathResource("graphql-test/api/videoContentByImdbIdTest.graphql");
        videoContentByImdbIdQuery = new String(Files.readAllBytes(Paths.get(videoContentByImdbQueryResource.getURI())));
        //Creating new videoContent entity
        videoContent = new VideoContent(
                new VideoContent.BasicInfo(
                        1L,
                        new VideoContentTitle("Title 1"),
                        "Poster 1",
                        VideoContentCategory.ANIME,
                        VideoContentFormat.MOVIE,
                        2020,
                        new ExternalId(12, 23, 34, "tt0000045")
                ),
                new VideoContent.ExternalInfo(
                        VideoContentStatus.ONGOING,
                        "Some beautiful description",
                        LocalDate.of(2022, 2, 24),
                        LocalDate.of(2077, 10, 23),
                        12345,
                        7,
                        13,
                        Arrays.stream(new String[]{"War", "Drama", "Apocalypse"}).toList(),
                        Arrays.stream(new String[]{"War", "Genocide", "Fire"}).toList(),
                        6.66f,
                        Arrays.stream(new Studio[]{new Studio("Militech"), new Studio("Vault-tech")}).toList(),
                        null,
                        null
                )
        );
    }

    public void checkVideoContent(GraphQlTester.Response response, String rootName){
        response
                .path(rootName + ".basicInfo.id").entity(Long.class).isEqualTo(videoContent.basicInfo().id())
                .path(rootName + ".basicInfo.title.english").entity(String.class).isEqualTo(videoContent.basicInfo().title().english())
                .path(rootName + ".basicInfo.year").entity(Integer.class).isEqualTo(videoContent.basicInfo().year())
                .path(rootName + ".basicInfo.category").entity(VideoContentCategory.class).isEqualTo(videoContent.basicInfo().category())
                .path(rootName + ".basicInfo.format").entity(VideoContentFormat.class).isEqualTo(videoContent.basicInfo().format())
                .path(rootName + ".basicInfo.poster").entity(String.class).isEqualTo(videoContent.basicInfo().poster())
                .path(rootName + ".basicInfo.externalIds.tmdbId").entity(Integer.class).isEqualTo(videoContent.basicInfo().externalIds().tmdbId())
                .path(rootName + ".basicInfo.externalIds.aniListId").entity(Integer.class).isEqualTo(videoContent.basicInfo().externalIds().aniListId())
                .path(rootName + ".basicInfo.externalIds.malId").entity(Integer.class).isEqualTo(videoContent.basicInfo().externalIds().malId())
                .path(rootName + ".basicInfo.externalIds.imdbId").entity(String.class).isEqualTo(videoContent.basicInfo().externalIds().imdbId())
                .path(rootName + ".externalInfo.synonyms").entityList(String.class).hasSize(3)
                .path(rootName + ".externalInfo.meanScore").entity(Float.class).isEqualTo(videoContent.externalInfo().meanScore())
                .path(rootName + ".externalInfo.studios").entityList(Studio.class).hasSize(2)
                .path(rootName + ".externalInfo.status").entity(VideoContentStatus.class).isEqualTo(videoContent.externalInfo().status())
                .path(rootName + ".externalInfo.startDate").entity(LocalDate.class).isEqualTo(videoContent.externalInfo().startDate())
                .path(rootName + ".externalInfo.endDate").entity(LocalDate.class).isEqualTo(videoContent.externalInfo().endDate())
                .path(rootName + ".externalInfo.episodes").entity(Integer.class).isEqualTo(videoContent.externalInfo().episodes())
                .path(rootName + ".externalInfo.seasons").entity(Integer.class).isEqualTo(videoContent.externalInfo().seasons())
                .path(rootName + ".externalInfo.duration").entity(Integer.class).isEqualTo(videoContent.externalInfo().duration())
                .path(rootName + ".externalInfo.genres").entityList(String.class).hasSize(3)
                .path(rootName + ".externalInfo.description").entity(String.class).isEqualTo(videoContent.externalInfo().description())
                .path(rootName + ".externalInfo.networks").valueIsNull()
                .path(rootName + ".externalInfo.recommendations").valueIsNull();
    }

    //VideoContent by Content ID
    @Test
    public void videoContentFoundById() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentById(1L, true)).thenReturn(videoContent);

        checkVideoContent(tester.document(videoContentQuery).execute(), "videoContent");
    }

    @Test
    public void videoContentNotFoundById() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentById(1L, true)).thenThrow(new ContentNotFoundException());

        tester.document(videoContentQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoNotFoundById() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentById(1L, true)).thenThrow(new ExternalInfoException());

        tester.document(videoContentQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContent.basicInfo").valueIsNull()
                .path("videoContent.externalInfo").valueIsNull();
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoFoundById() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentById(1L, true)).thenThrow(new ExternalInfoException(videoContent.basicInfo()));

        tester.document(videoContentQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContent").hasValue()
                .path("videoContent.basicInfo").hasValue()
                .path("videoContent.externalInfo").valueIsNull();
    }

    //VideoContent By AniList ID
    @Test
    public void videoContentFoundByAniListId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByAniListId(12, true)).thenReturn(videoContent);

        checkVideoContent(tester.document(videoContentByAniListIdQuery).execute(), "videoContentByAniListId");
    }

    @Test
    public void videoContentNotFoundByAniListId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByAniListId(12, true)).thenThrow(new ContentNotFoundException());

        tester.document(videoContentByAniListIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoNotFoundByAniListId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByAniListId(12, true)).thenThrow(new ExternalInfoException());

        tester.document(videoContentByAniListIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByAniListId.basicInfo").valueIsNull()
                .path("videoContentByAniListId.externalInfo").valueIsNull();
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoFoundByAniListId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByAniListId(12, true)).thenThrow(new ExternalInfoException(videoContent.basicInfo()));

        tester.document(videoContentByAniListIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByAniListId").hasValue()
                .path("videoContentByAniListId.basicInfo").hasValue()
                .path("videoContentByAniListId.externalInfo").valueIsNull();
    }

    //VideoContent By TMDB ID
    @Test
    public void videoContentFoundByTmdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByTmdbId(23, VideoContentFormat.MOVIE, true)).thenReturn(videoContent);

        checkVideoContent(tester.document(videoContentByTmdbIdQuery).execute(), "videoContentByTmdbId");
    }

    @Test
    public void videoContentNotFoundByTmdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByTmdbId(23, VideoContentFormat.MOVIE, true)).thenThrow(new ContentNotFoundException());

        tester.document(videoContentByTmdbIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoNotFoundByTmdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByTmdbId(23, VideoContentFormat.MOVIE, true)).thenThrow(new ExternalInfoException());

        tester.document(videoContentByTmdbIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByTmdbId.basicInfo").valueIsNull()
                .path("videoContentByTmdbId.externalInfo").valueIsNull();
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoFoundByTmdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByTmdbId(23, VideoContentFormat.MOVIE, true)).thenThrow(new ExternalInfoException(videoContent.basicInfo()));

        tester.document(videoContentByTmdbIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByTmdbId").hasValue()
                .path("videoContentByTmdbId.basicInfo").hasValue()
                .path("videoContentByTmdbId.externalInfo").valueIsNull();
    }

    //VideoContent By MAL ID
    @Test
    public void videoContentFoundByMalId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByMalId(34, true)).thenReturn(videoContent);

        checkVideoContent(tester.document(videoContentByMalIdQuery).execute(), "videoContentByMalId");
    }

    @Test
    public void videoContentNotFoundByMalId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByMalId(34, true)).thenThrow(new ContentNotFoundException());

        tester.document(videoContentByMalIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoNotFoundByMalId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByMalId(34, true)).thenThrow(new ExternalInfoException());

        tester.document(videoContentByMalIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByMalId.basicInfo").valueIsNull()
                .path("videoContentByMalId.externalInfo").valueIsNull();
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoFoundByMalId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByMalId(34, true)).thenThrow(new ExternalInfoException(videoContent.basicInfo()));

        tester.document(videoContentByMalIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByMalId").hasValue()
                .path("videoContentByMalId.basicInfo").hasValue()
                .path("videoContentByMalId.externalInfo").valueIsNull();
    }

    //VideoContent By IMDB ID
    @Test
    public void videoContentFoundByImdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByImdbId("tt0000045", true)).thenReturn(videoContent);

        checkVideoContent(tester.document(videoContentByImdbIdQuery).execute(), "videoContentByImdbId");
    }

    @Test
    public void videoContentNotFoundByImdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByImdbId("tt0000045", true)).thenThrow(new ContentNotFoundException());

        tester.document(videoContentByImdbIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("NOT_FOUND");
                });
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoNotFoundByImdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByImdbId("tt0000045", true)).thenThrow(new ExternalInfoException());

        tester.document(videoContentByImdbIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByImdbId.basicInfo").valueIsNull()
                .path("videoContentByImdbId.externalInfo").valueIsNull();
    }

    @Test
    public void videoContentExternalInfoExceptionBasicInfoFoundByImdbId() throws ContentNotFoundException, ExternalInfoException {
        when(videoContentService.getVideoContentByImdbId("tt0000045", true)).thenThrow(new ExternalInfoException(videoContent.basicInfo()));

        tester.document(videoContentByImdbIdQuery)
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.getFirst().getErrorType().toString()).isEqualTo("INTERNAL_ERROR");
                })
                .path("videoContentByImdbId").hasValue()
                .path("videoContentByImdbId.basicInfo").hasValue()
                .path("videoContentByImdbId.externalInfo").valueIsNull();
    }
}
