package com.writenbite.bisonfun.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.writenbite.bisonfun.api.client.anilist.AniListApiResponse;
import com.writenbite.bisonfun.api.config.ModelConfig;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbTrending;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.tools.model.time.TimeWindow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "bisonfun.rate-limit.requests-per-second=4",
        "bisonfun.rate-limit.requests-per-minute=6"
})
@AutoConfigureHttpGraphQlTester
public class RateLimitingTest {
    @MockBean
    private AniListApiResponse aniListApiResponse;
    @MockBean
    private TmdbApi tmdbApi;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TmdbTrending tmdbTrending;

    private String trendsRequest;

    @BeforeEach
    public void setUp() throws IOException {
        trendsRequest = readResourceFile("graphql-test/api/trendingTest.graphql");

        clearAllCaches();
    }

    @AfterEach
    public void tearDown() {
        clearAllCaches();
    }

    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> Optional.ofNullable(cacheManager.getCache(cacheName)).ifPresent(Cache::clear));
    }

    private String readResourceFile(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }

    @Test
    public void givenRedisCaching_whenMovieTrendsReachedMaximumLimitPerMinute_thenMovieTrendsReturnTooManyRequestsError() throws Exception {
        MovieResultsPage movieTrends = ModelConfig.ofTmdbApi(MovieResultsPage.class).create();
        when(tmdbApi.getTrending()).thenReturn(tmdbTrending);
        when(tmdbTrending.getMovies(TimeWindow.WEEK, null)).thenReturn(movieTrends);

        Map<String, String> requestBodyMap = new HashMap<>();
        requestBodyMap.put("query", trendsRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);

        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 3; i++) {
                log.info("Request #{}", (j * 2) + (i + 1));
                mockMvc.perform(post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                        )
                        .andExpect(status().isOk());
            }
            Thread.sleep(Duration.ofSeconds(1).toMillis());
        }
        mockMvc.perform(post("/graphql")
                        .content(requestBody)
                        .contentType("application/json"))
                .andExpect(status().isTooManyRequests());
    }
}
