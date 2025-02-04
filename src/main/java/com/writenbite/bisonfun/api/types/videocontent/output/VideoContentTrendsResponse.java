package com.writenbite.bisonfun.api.types.videocontent.output;

import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

import java.util.List;

public record VideoContentTrendsResponse(
        VideoContentCategory category,
        VideoContentFormat format,
        List<VideoContent.BasicInfo> trends
) {
}
