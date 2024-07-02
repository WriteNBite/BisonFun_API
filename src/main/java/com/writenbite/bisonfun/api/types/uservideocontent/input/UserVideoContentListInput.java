package com.writenbite.bisonfun.api.types.uservideocontent.input;

import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

import java.util.List;

public record UserVideoContentListInput(
        Integer episode,
        Integer score,
        List<UserVideoContentListStatus> statuses,
        List<VideoContentCategory> categories,
        List<VideoContentFormat> formats,
        Integer yearFrom,
        Integer yearTo
) {
}
