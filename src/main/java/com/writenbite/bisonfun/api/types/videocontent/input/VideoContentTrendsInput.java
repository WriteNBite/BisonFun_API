package com.writenbite.bisonfun.api.types.videocontent.input;

import com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

public record VideoContentTrendsInput(
        VideoContentCategory category,
        VideoContentFormat format
) {
}
