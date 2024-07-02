package com.writenbite.bisonfun.api.types.videocontent.output;

import com.writenbite.bisonfun.api.types.videocontent.VideoContent;

import java.util.List;

public record TrendVideoContentResponse(
        List<VideoContent.BasicInfo> animeTrends,
        List<VideoContent.BasicInfo> movieTrends,
        List<VideoContent.BasicInfo> tvTrends
) {
}
