package com.writenbite.bisonfun.api.config;

import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.user.User;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UserVideoContentListConnection;
import com.writenbite.bisonfun.api.types.videocontent.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Random;

@Configuration
public class UserVideoContentListConnectionConfig {
    @Bean
    public UserVideoContentListConnection contentListConnection() {
        Random random = new Random();
        User user = new User(1, "tester");
        List<UserVideoContentListElement> userList = getMovies().stream()
                .map(movie -> new UserVideoContentListElement(user, movie, random.nextInt(11), random.nextInt(11), UserVideoContentListStatus.values()[random.nextInt(UserVideoContentListStatus.values().length)]))
                .toList();
        return new UserVideoContentListConnection(userList, new PageInfo(userList.size(), userList.size(), 1, 1, false));
    }

    private static List<VideoContent.BasicInfo> getMovies() {
        return List.of(
                new VideoContent.BasicInfo(
                        1L,
                        new VideoContentTitle("Title 1"),
                        "Poster 1 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2022,
                        new ExternalId(12, 13, 14, "tt0000015")
                ),
                new VideoContent.BasicInfo(
                        2L,
                        new VideoContentTitle("Title 2"),
                        "Poster 2 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2021,
                        new ExternalId(22, 23, 24, "tt0000025")
                ),
                new VideoContent.BasicInfo(
                        3L,
                        new VideoContentTitle("Title 3"),
                        "Poster 3 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2020,
                        new ExternalId(32, 33, 34, "tt0000035")
                ),
                new VideoContent.BasicInfo(
                        4L,
                        new VideoContentTitle("Title 4"),
                        "Poster 4 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2019,
                        new ExternalId(42, 43, 44, "tt0000045")
                ),
                new VideoContent.BasicInfo(
                        5L,
                        new VideoContentTitle("Title 5"),
                        "Poster 5 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2018,
                        new ExternalId(52, 53, 54, "tt0000055")
                ),
                new VideoContent.BasicInfo(
                        6L,
                        new VideoContentTitle("Title 6"),
                        "Poster 6 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2017,
                        new ExternalId(62, 63, 64, "tt0000065")
                ),
                new VideoContent.BasicInfo(
                        7L,
                        new VideoContentTitle("Title 7"),
                        "Poster 7 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2016,
                        new ExternalId(72, 73, 74, "tt0000075")
                ),
                new VideoContent.BasicInfo(
                        8L,
                        new VideoContentTitle("Title 8"),
                        "Poster 8 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2015,
                        new ExternalId(82, 83, 84, "tt0000085")
                ),
                new VideoContent.BasicInfo(
                        9L,
                        new VideoContentTitle("Title 9"),
                        "Poster 9 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2014,
                        new ExternalId(92, 93, 94, "tt0000095")
                ),
                new VideoContent.BasicInfo(
                        10L,
                        new VideoContentTitle("Title 10"),
                        "Poster 10 URL",
                        VideoContentCategory.MAINSTREAM,
                        VideoContentFormat.MOVIE,
                        2013,
                        new ExternalId(102, 103, 104, "tt0000105")
                )
        );
    }
}
